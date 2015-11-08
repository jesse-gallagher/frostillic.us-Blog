module Domino
	class ViewEntryCollection < Base
		attr_reader :parent
		attr_accessor :read_column_names
		
		#def initialize(stats, entries)
		#	@stats = stats
		#	@entries = entries
		#end
		
		def initialize(parent, position, return_count=0xFFFFFFFF, key_matches=-1, read_mask=nil)
			super
			
			@parent = parent
			@position = position
			@return_count = return_count
			@key_matches = key_matches
			@read_mask = read_mask == nil ? default_read_mask : read_mask
			
			@read_column_names = false
		end
		
		def count
			@key_matches > -1 ? @key_matches : @parent.actual_entry_count
		end
		
		def each
			hBuffer = FFI::MemoryPointer.new(:int)
			entries_found = FFI::MemoryPointer.new(:int)
			signal_flags = FFI::MemoryPointer.new(:int)
			notes_found = 0
			#stats = nil
			#entries_list = []
			begin
				result = API.NIFReadEntries(
					@parent.collection_handle,
					@position.to_ptr,
					@parent.ft_searched? ? API::NAVIGATE_NEXT_HIT : API::NAVIGATE_NEXT,
					@position[:Tumbler][0] == 0 ? 1 : 0,
					@parent.ft_searched? ? API::NAVIGATE_NEXT_HIT : API::NAVIGATE_NEXT,
					@return_count,
					@read_mask,
					hBuffer,
					nil,
					nil,
					entries_found,
					signal_flags
				)
				if result != 0
					raise NotesException.new(result)
				end
				if hBuffer.read_int == API::NULLHANDLE
					raise Exception.new("Empty buffer returned by NIFReadEntries.")
				end
				info_ptr = API.OSLockObject(hBuffer.read_int)
				
				@stats = API::COLLECTIONSTATS.new(info_ptr)
				info_ptr += API::COLLECTIONSTATS.size
				
				# loop through all read entries
				1.upto(entries_found.read_int) do
					notes_found += 1
					#entry.index = notes_found
					#entry = ViewEntry.read(self, notes_found, info_ptr, read_mask)
					entry = ViewEntry.new
					info_ptr = entry.read self, notes_found, info_ptr, @read_mask
					
					#entries_list << entry.freeze
					yield entry
				end
				
				API.OSUnlockObject hBuffer.read_int
				API.OSMemFree hBuffer.read_int
			end while signal_flags.read_int & API::SIGNAL_MORE_TO_DO != 0
		end
		
		private
		def default_read_mask
			API::READ_MASK_COLLECTIONSTATS +
				API::READ_MASK_NOTEID + API::READ_MASK_NOTEUNID + API::READ_MASK_NOTECLASS +
				API::READ_MASK_INDEXSIBLINGS + API::READ_MASK_INDEXCHILDREN + API::READ_MASK_INDEXDESCENDANTS +
				API::READ_MASK_INDEXANYUNREAD + API::READ_MASK_INDENTLEVELS +
				(@parent.ft_searched? ? API::READ_MASK_SCORE : 0) +
				API::READ_MASK_INDEXUNREAD + API::READ_MASK_INDEXPOSITION +
				(@read_column_names ? API::READ_MASK_SUMMARY : API::READ_MASK_SUMMARYVALUES)
		end
	end
end
