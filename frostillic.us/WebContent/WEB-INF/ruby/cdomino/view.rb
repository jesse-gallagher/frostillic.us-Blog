module Domino
	class View < Document
		attr_reader :handle, :parent, :collection_handle
		
=begin
		def initialize(parent, handle, noteid)
			@parent = parent
			@handle = handle
			@noteid = noteid
			
			@collection_data = false
			@ft_searched = false
		end
=end
		def initialize(parent, handle)
			super
			
			collection_handle_ptr = FFI::MemoryPointer.new(API.find_type(:HCOLLECTION))
			result = API.NIFOpenCollection(
				@parent.handle,
				@parent.handle,
				noteid,
				0,
				API::NULLHANDLE,
				collection_handle_ptr,
				nil,
				nil,
				nil,
				nil
			)
			raise NotesException.new(result) if result != 0
			
			@collection_handle = collection_handle_ptr.read_uint16
		end
		
		def collation
			coll_num = FFI::MemoryPointer.new(:uint16)
			result = API.NIFGetCollation(@collection_handle, coll_num)
			raise NotesException.new(result) if result != 0
			coll_num.read_uint16
		end
		def collation=(coll_num)
			result = API.NIFSetCollation(@collection_handle, coll_num)
			raise NotesException.new(result) if result != 0
		end
		
		def update
			result = API.NIFUpdateCollection(@collection_handle)
			raise NotesException.new(result) if result != 0
		end
		
		def doc_count
			fetch_collection_data! if not @collection_data
			@collection_data[:DocCount]
		end
		def entry_size
			fetch_collection_data! if not @collection_data
			@collection_data[:DocTotalSize]
		end
		def actual_entry_count
			if ft_searched?
				@ft_search_count
			else
				doc_count
			end
		end
		
		def ft_search!(query, max_docs=0)
			# to search, first create a search handle, then execute the search
			
			close_search!
			
			handle_ptr = FFI::MemoryPointer.new(:uint)
			result = API.FTOpenSearch(handle_ptr)
			raise NotesException.new(result) if result != 0
			@search_handle = handle_ptr.read_int
			
			num_docs = FFI::MemoryPointer.new(:uint32)
			results_handle = FFI::MemoryPointer.new(:uint32)
			
			result = API.FTSearch(
				@parent.handle,
				handle_ptr,
				@collection_handle,
				query.to_s,
				API::FT_SEARCH_SET_COLL | API::FT_SEARCH_SCORES,
				max_docs,
				API::NULLHANDLE,
				num_docs,
				nil,
				results_handle
			)
			raise NotesException.new(result) if result != 0
			
			@ft_searched = true
			@ft_search_count = num_docs.read_uint32
			
			@ft_search_count
		end
		def ft_searched?; @ft_searched; end
		def ft_search_count(query, max_docs=0)
			handle_ptr = FFI::MemoryPointer.new(:uint)
			result = API.FTOpenSearch(handle_ptr)
			raise NotesException.new(result) if result != 0
			
			num_docs = FFI::MemoryPointer.new(:uint32)
			results_handle = FFI::MemoryPointer.new(:uint32)
			result = API.FTSearch(
				@parent.handle,
				handle_ptr,
				@collection_handle,
				query.to_s,
				API::FT_SEARCH_SET_COLL | API::FT_SEARCH_NUMDOCS_ONLY,
				max_docs,
				API::NULLHANDLE,
				num_docs,
				nil,
				results_handle
			)
			raise NotesException.new(result) if result != 0
			
			API.FTCloseSearch(handle_ptr.read_int)
			
			num_docs.read_uint32
		end
		
		def documents
			hTable_ptr = FFI::MemoryPointer.new(API.find_type(:DHANDLE))
			result = API.IDCreateTable(0, hTable_ptr)
			raise NotesException.new(result) if result != 0
			hTable = hTable_ptr.read_uint32
			
			# Walk the view to get the IDs
			position = API::COLLECTIONPOSITION.new
			position[:Level] = 0
			position[:Tumbler][0] = 0
			
			entries = ViewEntryCollection.new(self, position, 0xFFFFFFFF, -1, API::READ_MASK_NOTEID)
			entries.each do |entry|
				result = API.IDInsert(hTable, entry.noteid, nil)
				raise NotesException.new(result) if result != 0
			end
			
			add_child DocumentCollection.new(@self, hTable)
		end
		
		def entries
			position = API::COLLECTIONPOSITION.new
			position[:Level] = 0
			position[:Tumbler][0] = 0
			
			add_child ViewEntryCollection.new(self, position)
		end
		
		def entries_by_key(key, exact=false)
			if not key.is_a? Array
				key = [key]
			end
			
			position = API::COLLECTIONPOSITION.new
			position[:Level] = 0
			position[:Tumbler][0] = 0
			num_matches = FFI::MemoryPointer.new(API.find_type(:DWORD))
			
			# Create an ITEM_TABLE to pass in
			table_ptr = API.create_nameless_item_table(key)
			
			result = API.NIFFindByKey(
				@collection_handle,
				table_ptr,
				API::FIND_FIRST_EQUAL |
					API::FIND_CASE_INSENSITIVE |
					API::FIND_ACCENT_INSENSITIVE |
					API::FIND_RETURN_DWORD |
					(exact ? 0 : API::FIND_PARTIAL),
				position,
				num_matches
			)
			raise NotesException.new(result) if result > 0
			
			add_child ViewEntryCollection.new(self, position, num_matches.read_uint32, num_matches.read_uint32)
		end
		
		def close
			if not self.closed?
				super
				close_search!
				result = API.NIFCloseCollection @collection_handle
				raise NotesException.new(result) if result != 0
			end
		end
		
		private
		def fetch_collection_data!
			data_handle = FFI::MemoryPointer.new(:int)
			result = API.NIFGetCollectionData(@collection_handle, data_handle)
			raise NotesException.new(result) if result != 0
			data_ptr = API.OSLockObject(data_handle.read_int)
			@collection_data = API::COLLECTIONDATA.new(data_ptr)
			API.OSUnlockObject(data_handle.read_int)
		end
		def close_search!
			API.FTCloseSearch @search_handle if @search_handle
			@search_handle = nil
			@ft_search_count = 0
			@ft_searched = false
		end
	end
end