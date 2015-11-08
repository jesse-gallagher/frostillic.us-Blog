module Domino
	class Database < Base
		attr_reader :handle
		
		def initialize(handle)
			super
			
			@handle = handle
		end
		
		def title
			if @dbinfo == nil
				dbinfo = FFI::MemoryPointer.from_string(" " * 128)
				result = API.NSFDbInfoGet(@handle, dbinfo)
				if result != 0
					raise NotesException.new(result)
				end
				@dbinfo = dbinfo.read_string
				@title = @dbinfo.split("\n").first
			end
			@title	
		end
		
		def filepath
			fetch_filepath! if @filepath == nil
			@filepath
		end
		def server
			fetch_filepath! if @server == nil
			@server
		end
		def replicaid
			fetch_replica_info! if @replicaid == nil
			@replicaid
		end

		# TODO: fix the segfault this causes
=begin
		def get_user_info(username)
			names_list_handle = FFI::MemoryPointer.new(:int)
			result = API.NSFNamesList(username, 0, names_list_handle)
			if result == 0
				API.OSLockObject names_list_handle.read_int
				# now get the info from the ACL, if that's how it works
				result = API.NSFDbGetNamesList(@handle, 0, names_list_handle)
				if result == 0
					# fetched names list
					API::NameInfo.new(names_list_handle.read_int)
				else
					raise NotesException.raise(result)
				end
				
			else
				raise NotesException.raise(result)
			end
		end
=end
    
		def get_view(view_name)
			self.get_design_note(view_name, API::DFLAGPAT_VIEWS_AND_FOLDERS, View)
		end
=begin
		def get_view_as_user(viewname, username)
			names_list = FFI::MemoryPointer.new(:int, API::NAMES_LIST.size + 100)
			result = API.NSFBuildNamesList(nil, 0, names_list)
			if result == 0
				names_list_obj = API::NAMES_LIST.new(API.OSLockObject(names_list.read_int))
				names_list_obj[:authenticated] = 0
				API.OSUnlockObject(names_list.read_int)
				view_noteid = FFI::MemoryPointer.new(:int)
				result = API.find_view(@handle, viewname.to_s, view_noteid)
				if result == 0
					handle = FFI::MemoryPointer.new(:int)
					result = API.NIFOpenCollectionWithUserNameList(@handle, @handle, view_noteid.read_int, 0, API::NULLHANDLE, handle, nil, nil, nil, nil, names_list.read_int)
					if result != 0
						raise NotesException.new(result)
					end
					View.new(self, handle.read_int)
				else
					raise NotesException.new(result)
				end
				
			else
				raise NotesException.new(result)
			end
		end
=end
		
		def get_form(form_name)
			self.get_design_note(form_name, API::DFLAGPAT_FORM_OR_SIMILAR, Form)
		end
		
		def get_design_note(note_name, filter, doc_class=Document)
			noteid_ptr = FFI::MemoryPointer.new(API.find_type(:NOTEID))
			result = API.NIFFindDesignNoteExt(@handle, note_name.to_s, API::NOTE_CLASS_ALL, filter.to_s, noteid_ptr, 0)
			raise NotesException.new(result) if result != 0
			
			self.doc_by_id(noteid_ptr.read_uint32, doc_class)
		end
		
		def doc_by_id(noteid, doc_class=Document)
			modified_ptr = FFI::MemoryPointer.new(API::TIMEDATE)
			note_class_ptr = FFI::MemoryPointer.new(API.find_type(:WORD))
			originatorid_ptr = FFI::MemoryPointer.new(API::ORIGINATORID)
			
			# Get some header info
			result = API.NSFDbGetNoteInfo(@handle, noteid, originatorid_ptr, modified_ptr, note_class_ptr)
			raise NotesException.new(result) if result != 0
			
			originatorid = API::OriginatorID.new(originatorid_ptr)
			modified = API::TIMEDATE.new(modified_ptr)
			note_class = note_class_ptr.read_uint16
			
			# Open the note itself
			handle_ptr = FFI::MemoryPointer.new(API.find_type(:NOTEHANDLE))
			result = API.NSFNoteOpenExt(@handle, noteid, API::OPEN_RAW_MIME, handle_ptr)
			raise NotesException.new(result) if result != 0
			
			
			#add_child doc_class.new(self, handle_ptr.read_uint32, noteid, originatorid, modified, note_class)
			add_child doc_class.new(self, handle_ptr.read_uint32)
		end
		def doc_by_unid(unid, doc_class=Document)
			if not unid.is_a?(API::UNIVERSALNOTEID)
				unid = API::UNIVERSALNOTEID.from_s(unid.to_s)
			end
			
			noteid_ptr = FFI::MemoryPointer.new(API.find_type(:NOTEID))
			originatorid_ptr = FFI::MemoryPointer.new(API::ORIGINATORID)
			modified_ptr = FFI::MemoryPointer.new(API::TIMEDATE)
			note_class_ptr = FFI::MemoryPointer.new(API.find_type(:WORD))
			# Get some header info
			result = API.NSFDbGetNoteInfoByUNID(@handle, unid.to_ptr, noteid_ptr, originatorid_ptr, modified_ptr, note_class_ptr)
			raise NotesException.new(result) if result != 0
			
			noteid = noteid_ptr.read_uint32
			originatorid = API::OriginatorID.new(originatorid_ptr)
			modified = API::TIMEDATE.new(modified_ptr)
			note_class = note_class_ptr.read_uint16
			
			# Open the note itself
			handle_ptr = FFI::MemoryPointer.new(API.find_type(:NOTEHANDLE))
			result = API.NSFNoteOpenExt(@handle, noteid, API::OPEN_RAW_MIME, handle_ptr)
			raise NotesException.new(result) if result != 0
			
			#add_child doc_class.new(self, handle_ptr.read_uint32, noteid, originatorid, modified, note_class)
			add_child doc_class.new(self, handle_ptr.read_uint32)
		end
		
		def create_document(note_class=API::NOTE_CLASS_DOCUMENT)
			note_handle_ptr = FFI::MemoryPointer.new(API.find_type(:NOTEHANDLE))
			result = API.NSFNoteCreate(@handle, note_handle_ptr)
			raise NotesException.new(result) if result != 0
			note_handle = note_handle_ptr.read_uint32
			
			# Set the note's class
			note_class_ptr = FFI::MemoryPointer.new(API.find_type(:WORD))
			note_class_ptr.write_uint16(note_class)
			API.NSFNoteSetInfo note_handle, API::F_NOTE_CLASS, note_class_ptr
			
			#add_child Document.new(self, note_handle, noteid, originatorid, modified, note_class)
			add_child Document.new(self, note_handle)
		end
		
		def to_html(options=nil)
			Session.html_converter(options) do |converter|
				result = API.HTMLConvertNote(converter, @handle, 0, 0, nil)
				raise NotesException.new(result) if result != 0
			end
		end
		
		def to_dxl(properties=nil)
			dxl = ""
			
			hDXLExport_ptr = FFI::MemoryPointer.new(API.find_type(:DXLEXPORTHANDLE))
			result = API.DXLCreateExporter(hDXLExport_ptr)
			raise NotesException.new(result) if result != 0
			hDXLExport = hDXLExport_ptr.read_uint32
			
			# Set any options
			if properties != nil and properties.is_a? Hash
				properties.each do |key, value|
					value_ptr = nil
					if not value.is_a? FFI::Pointer
						# Then it can only legally be a boolean or string
						if value.is_a? TrueClass
							value_ptr = FFI::MemoryPointer.new(API.find_type(:BOOL))
							value_ptr.write_uint32(1)
						elsif value.is_a? FalseClass
							value_ptr = FFI::MemoryPointer.new(API.find_type(:BOOL))
							value_ptr.write_uint32(0)
						else
							value_ptr = FFI::MemoryPointer.from_string(value.to_s)
						end
					else
						value_ptr = value
					end
					
					result = API.DXLSetExporterProperty(hDXLExport, key, value_ptr)
				end
			end
			
			process_xml_block = Proc.new do |pBuffer, length, pAction|
				dxl += pBuffer.read_string(length)
			end
			
			result = API.DXLExportDatabase(hDXLExport, process_xml_block, @handle, nil)
			raise NotesException.new(result) if result != 0
			
			API.DXLDeleteExporter(hDXLExport)
			
			dxl
		end
		
		def close
			if not self.closed?
				super
				
				API.NSFDbClose(@handle)
			end
		end
		
		private
		def fetch_filepath!
			canonical = FFI::MemoryPointer.from_string(" " * API::MAXPATH)
			expanded = FFI::MemoryPointer.from_string(" " * API::MAXPATH)
			result = API.NSFDbPathGet(@handle, canonical, expanded)
			if result != 0
				raise NotesException.new(result)
			end
			@canonical_filepath = canonical.read_string
			@expanded_filepath = expanded.read_string
			if @expanded_filepath["!!"]
				bits = @expanded_filepath.split("!!")
				@filepath = bits[1]
				@server = bits[0]
			else
				@filepath = @expanded_filepath
				@server = ""
			end
		end
		def fetch_replica_info!
			replica_info = API::DBREPLICAINFO.new
			result = API.NSFDbReplicaInfoGet(@handle, replica_info.to_ptr)
			raise NotesException.new(result) if result != 0
			
			@replicaid = replica_info[:ID].to_replicaid
			@replica_flags = replica_info[:Flags]
			@cutoff_interval = replica_info[:CutoffInterval]
			@cutoff = replica_info[:Cutoff].to_t
		end
	end
end