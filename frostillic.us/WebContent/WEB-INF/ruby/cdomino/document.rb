module Domino
	class Document < Base
		attr_reader :handle, :parent, :noteid, :universalid, :modified, :note_class, :sequence, :sequence_time
		
		#def initialize(parent, handle, noteid, originatorid, modified, note_class)
		def initialize(parent, handle)
			super
			
			@parent = parent
			@handle = handle
			#@noteid = noteid
			#@universalid = originatorid.universalid
			#@modified = modified.to_t
			#@note_class = note_class
			#@sequence = originatorid.sequence
			#@sequence_time = originatorid.sequence_time.to_t
			
			update_note_info!
		end
		
		# TODO: make this work
		def convert_to_mime!
			if !@converted_to_mime
				#cc_handle = FFI::MemoryPointer.new(API.find_type(:CCHANDLE))
				#result = API.MMCreateConvControls(cc_handle)
				#raise NotesException.new(result) if result > 0
				result = API.MIMEConvertCDParts(@handle, canonical? ? 1 : 0, 1, nil)
				raise NotesException.new(result) if result > 0
				#result = API.MMDestroyConvControls(cc_handle)
				#raise NotesException.new(result) if result > 0
			end
			nil
		end
		
		def read_only?; get_note_info(API::F_NOTE_FLAGS) & API::NOTE_FLAG_READONLY > 0; end
		def abstracted?; get_note_info(API::F_NOTE_FLAGS) & API::NOTE_FLAG_ABSTRACTED > 0; end
		def incremental?; get_note_info(API::F_NOTE_FLAGS) & API::NOTE_FLAG_INCREMENTAL > 0; end
		def linked?; get_note_info(API::F_NOTE_FLAGS) & API::NOTE_FLAG_LINKED > 0; end
		def incremental_full?; get_note_info(API::F_NOTE_FLAGS) & API::NOTE_FLAG_INCREMENTAL_FULL > 0; end
		def canonical?; get_note_info(API::F_NOTE_FLAGS) & API::NOTE_FLAG_CANONICAL > 0; end
		
		def accessed; get_note_info(API::F_NOTE_ACCESSED).to_t; end
		
		# These methods only work on databases that track heirarchy info
		def parent_id; get_note_info(API::F_NOTE_PARENT_NOTEID); end
		def response_count; get_note_info(API::F_NOTE_RESPONSE_COUNT); end
		# TODO: implement #responses
		
		def added; get_note_info(API::F_NOTE_ADDED_TO_FILE).to_t; end
		def obj_store; get_note_info(API::F_NOTE_OBJSTORE_DB); end
		
		def has_item?(item_name)
			result = API.NSFItemInfo(@handle, item_name, item_name.size, nil, nil, nil, nil)
			result == 0
		end
		def [](item_name)
			type_ptr = FFI::MemoryPointer.new(API.find_type(:WORD))
			blockid_ptr = FFI::MemoryPointer.new(API::BLOCKID)
			length_ptr = FFI::MemoryPointer.new(API.find_type(:DWORD))
			
			result = API.NSFItemInfo(@handle, item_name, item_name.size, nil, type_ptr, blockid_ptr, length_ptr)
			raise NotesException.new(result) if result != 0
			
			type = type_ptr.read_uint16
			blockid = API::BLOCKID.new(blockid_ptr)
			length = length_ptr.read_uint32
			
			# Blocks consist of an overall pool and an individual block
			# locking a block involves locking the pool and incrementing to the block
			block_ptr = API.OSLockObject(blockid[:pool]) + blockid[:block]
			if type == API::TYPE_COMPOSITE
				value = self.get_item_html(item_name)
			else
				value = API.read_item_value(block_ptr, length, @handle)
			end
			# unlocking a block only needs the pool ID
			API.OSUnlockObject(blockid[:pool])
			
			value.is_a?(Array) ? value : [value]
		end
		
		def []=(item_name, value)
			
			
			if value.is_a? Fixnum
				num_ptr = FFI::MemoryPointer.new(API.find_type(:NUMBER))
				num_ptr.write_double(value)
				result = API.NSFItemSetNumber(@handle, item_name.to_s, num_ptr)
				raise NotesException.new(result) if result != 0
			elsif value.is_a? Time
				value = value.utc
				time = API::TIMEDATE.from_t(value)
				
				# This wants a pointer to just the GM portion, so skip the pointer past the other parts
				#result = API.NSFItemSetTime(@handle, item_name.to_s, time.to_ptr + (API.find_type(:int).size * 10))
				result = API.NSFItemSetTime(@handle, item_name.to_s, time)
				raise NotesException.new(result) if result != 0
			elsif value.is_a? Date
				time = API::TIME.new
				time[:year] = value.year
				time[:month] = value.month
				time[:day] = value.day
				time[:hour] = time[:minute] = time[:second] = -1
				API.TimeLocalToGM(time.to_ptr)
				
				# This wants a pointer to just the GM portion, so skip the pointer past the other parts
				result = API.NSFItemSetTime(@handle, item_name.to_s, time.to_ptr + (API.find_type(:int).size * 10))
				raise NotesException.new(result) if result != 0
			elsif value.is_a? API::UNIVERSALNOTEID
				remove_item item_name
				
				# UNIDs are always stored as lists
				list = API::LIST.from_unid_array([value])
				result = API.NSFItemAppend(@handle, 0, item_name.to_s, item_name.to_s.size, API::TYPE_NOTEREF_LIST, list, list.total_size)
				raise NotesException.new(result) if result != 0
			elsif value.is_a? Array and value.length > 0 and value[0].is_a? Fixnum
				remove_item item_name
				
				# Then assume it's an array of numbers. If it's not, may god have mercy on us all
				range = API::RANGE.from_number_array(value)
				
				result = API.NSFItemAppend(@handle, API::ITEM_SUMMARY, item_name.to_s, item_name.to_s.size, API::TYPE_NUMBER_RANGE, range, range.total_size)
				raise NotesException.new(result) if result != 0
			elsif value.is_a? Array and value.length > 0 and value[0].is_a? API::UNIVERSALNOTEID
				remove_item item_name
				
				# It'd better be all UNIDs
				list = API::LIST.from_unid_array(value)
				
				result = API.NSFItemAppend(@handle, 0, item_name.to_s, item_name.to_s.size, API::TYPE_NOTEREF_LIST, list, list.total_size)
				raise NotesException.new(result) if result != 0
			elsif value.is_a? Array
				remove_item item_name
				
				value.each do |entry|
					val = entry.to_s
					result = API.NSFItemAppendTextList(@handle, item_name.to_s, val, val.size, 1)
					raise NotesException.new(result) if result != 0
				end
			else
				s = value.to_s.gsub("\n", "\0")
				result = API.NSFItemSetText(@handle, item_name.to_s, s, s.size)
				raise NotesException.new(result) if result != 0
			end
			value
		end
		
		def each_item
			process_item = Proc.new do |spare, flags, name, name_length, value, value_length, routine_param|
				item_name = name.read_bytes(name_length)
				
				# So... can I just read the item value from these parameters?
				type = value.read_uint16
				if type == API::TYPE_COMPOSITE
					# Rich text is a bag of hurt, so the best way may be to export it as DXL
					dxl = self.get_item_dxl(item_name)
					start = "<richtext>"
					start_index = dxl.index(start)
					if start_index != nil
						start_index += + start.length + 1 # for the newline
						end_index = dxl.index("</richtext>", start_index) - 1
						dxl = dxl[start_index..end_index]
					else
						dxl = ""
					end
					yield Item.new(self, item_name, type, dxl)
				else
					value = API.read_item_value(value, value_length, @handle)
					yield Item.new(self, item_name, type, value)
				end
			end
			result = API.NSFItemScan(@handle, process_item, nil)
			raise NotesException.new(result) if result != 0
		end
		
		def get_item_html(item_name)
			Session.html_converter do |converter|
				result = API.HTMLConvertItem(converter, @parent.handle, @handle, item_name.to_s)
				raise NotesException.new(result) if result != 0
			end
		end
		def get_item_dxl(item_name)
			item_list = API.create_text_list([item_name], false)
			dxl = self.to_dxl({
				:eRestrictToItemNames => item_list,
				:eForceNoteFormat => true
			})
			API.OSUnlockObject(item_list)
			API.OSMemFree(item_list)
			start = "<item name='#{item_name}'>"
			start_index = dxl.index(start)
			if start_index != nil
				start_index += start.length
				end_index = dxl.index("</item>", start_index) - 1
				dxl[start_index..end_index]
			else
				""
			end
		end
		
		def to_html(options=nil)
			Session.html_converter(options) do |converter|
				result = API.HTMLConvertNote(converter, @parent.handle, @handle, 0, nil)
				raise NotesException.new(result) if result != 0
			end
		end
		
		def to_dxl(properties=nil)
			dxl = ""
			
			hDXLExport = FFI::MemoryPointer.new(API.find_type(:DXLEXPORTHANDLE))
			result = API.DXLCreateExporter(hDXLExport)
			raise NotesException.new(result) if result != 0
			
			# Set any options
			if properties != nil and properties.is_a? Hash
				properties.each do |key, value|
					value_ptr = nil
					if not value.is_a? FFI::Pointer
						# Then it can only legally be a boolean, string, or handle
						if value.is_a? TrueClass
							value_ptr = FFI::MemoryPointer.new(API.find_type(:BOOL))
							value_ptr.write_uint32(1)
						elsif value.is_a? FalseClass
							value_ptr = FFI::MemoryPointer.new(API.find_type(:BOOL))
							value_ptr.write_uint32(0)
						elsif value.is_a? Fixnum
							value_ptr = FFI::MemoryPointer.new(API.find_type(:DHANDLE))
							value_ptr.write_uint32(value)
						else
							value_ptr = FFI::MemoryPointer.from_string(value.to_s)
						end
					else
						value_ptr = value
					end
					
					result = API.DXLSetExporterProperty(hDXLExport.read_uint32, key, value_ptr)
				end
			end
			
			process_xml_block = Proc.new do |pBuffer, length, pAction|
				dxl += pBuffer.read_string(length)
			end
			
			result = API.DXLExportNote(hDXLExport.read_uint32, process_xml_block, @handle, nil)
			raise NotesException.new(result) if result != 0
			
			API.DXLDeleteExporter(hDXLExport.read_uint32)
			
			dxl
		end
		
		def remove_item(item_name)
			s = item_name.to_s
			if has_item? item_name
				result = API.NSFItemDelete(@handle, s, s.size)
				raise NotesException.new(result) if result != 0
			end
		end
		
		def save(force=false)
			result = API.NSFNoteUpdateExtended(@handle, force ? API::UPDATE_FORCE : 0)
			raise NotesException.new(result) if result != 0
			
			update_note_info!
			
			true
		end
		
		def close
			if not self.closed?
				super
				
				API::NSFNoteClose @handle
			end
		end
		
		private
		def get_note_info(member)
			case member
			when API::F_NOTE_DB
				dbhandle = FFI::MemoryPointer.new(API.find_type(:DBHANDLE))
				API.NSFNoteGetInfo(@handle, member, dbhandle)
				return dbhandle.read_uint32
			when API::F_NOTE_ID
				noteid = FFI::MemoryPointer.new(API.find_type(:NOTEID))
				API.NSFNoteGetInfo(@handle, member, noteid)
				return noteid.read_uint32
			when API::F_NOTE_OID
				oid = FFI::MemoryPointer.new(API.ORIGINATORID)
				API.NSFNoteGetInfo(@handle, member, oid)
				return API::OriginatorID.new(oid)
			when API::F_NOTE_CLASS
				note_class = FFI::MemoryPointer.new(API.find_type(:WORD))
				API.NSFNoteGetInfo(@handle, member, note_class)
				return note_class.read_uint16
			when API::F_NOTE_MODIFIED
				modified = FFI::MemoryPointer.new(API::TIMEDATE)
				API.NSFNoteGetInfo(@handle, member, modified)
				return API::TIMEDATE.new(modified)
			when API::F_NOTE_PRIVILEGES
				priv = FFI::MemoryPointer.new(API.find_type(:WORD))
				API.NSFNoteGetInfo(@handle, member, priv)
				return priv.read_uint16
			when API::F_NOTE_FLAGS
				flags = FFI::MemoryPointer.new(API.find_type(:WORD))
				API.NSFNoteGetInfo(@handle, member, flags)
				return flags.read_uint16
			when API::F_NOTE_ACCESSED
				accessed = FFI::MemoryPointer.new(API::TIMEDATE)
				API.NSFNoteGetInfo(@handle, member, accessed)
				return API::TIMEDATE.new(accessed)
			when API::F_NOTE_PARENT_NOTEID
				noteid = FFI::MemoryPointer.new(API.find_type(:NOTEID))
				API.NSFNoteGetInfo(@handle, member, noteid)
				return noteid.read_uint32
			when API::F_NOTE_RESPONSE_COUNT
				response_count = FFI::MemoryPointer.new(API.find_type(:DWORD))
				API.NSFNoteGetInfo(@handle, member, response_count)
				return response_count.read_uint32
			when API::F_NOTE_RESPONSES
				# TODO: implement this - it'll have to be an IDTable
			when API::F_NOTE_ADDED_TO_FILE
				added = FFI::MemoryPointer.new(API::TIMEDATE)
				API.NSFNoteGetInfo(@handle, member, added)
				return API::TIMEDATE.new(added)
			when API::F_NOTE_OBJSTORE_DB
				dbhandle_ptr = FFI::MemoryPointer.new(API.find_type(:DBHANDLE))
				API.NSFNoteGetInfo(@handle, member, dbhandle)
				dbhandle = dbhandle_ptr.read_uint32
				if dbhandle == 0
					return nil
				else
					return Database.new(dbhandle.read_uint32)
				end
			end
		end
		
		def update_note_info!
			# Gather the generated info
			noteid_ptr = FFI::MemoryPointer.new(API.find_type(:NOTEID))
			API.NSFNoteGetInfo @handle, API::F_NOTE_ID, noteid_ptr
			@noteid = noteid_ptr.read_uint32
			
			oid = API::ORIGINATORID.new
			API.NSFNoteGetInfo @handle, API::F_NOTE_OID, oid.to_ptr
			originatorid = API::OriginatorID.new(oid.to_ptr)
			@universalid = originatorid.universalid
			@sequence = originatorid.sequence
			@sequence_time = originatorid.sequence_time.to_t
			
			modified = API::TIMEDATE.new
			API.NSFNoteGetInfo @handle, API::F_NOTE_MODIFIED, modified.to_ptr
			@modified = modified.to_t
			
			note_class_ptr = FFI::MemoryPointer.new(API.find_type(:WORD))
			API.NSFNoteGetInfo @handle, API::F_NOTE_CLASS, note_class_ptr
			@note_class = note_class_ptr.read_uint16
		end
	end
end