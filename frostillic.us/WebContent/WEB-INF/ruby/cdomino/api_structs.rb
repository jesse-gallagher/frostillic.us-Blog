require "ffi"
require "date"

module Domino
	module API
		class COLLECTIONPOSITION < FFI::Struct
			layout :Level, :WORD,
				:MinLevel, :BYTE,
				:MaxLevel, :BYTE,
				:Tumbler, [:DWORD, MAXTUMBLERLEVELS]
		end
		class ITEM_VALUE_TABLE < FFI::Struct
			layout :Length, :USHORT,
				:Items, :USHORT
			# This is followed by :items WORD values, which are the lengths of the data items
			# Then are the data items, each of which starts with a data type, which is a USHORT
			
			attr_reader :values
			# This convenience method will read in the table's values
			# This shouldn't happen automatically on construction, since Domino
			# may use "stub" versions from time to time, if I recall correctly
			def read_values!
				# add the size of ITEM_VALUE_TABLE to the pointer to get to the data value lengths
				summary_ptr = self.to_ptr + self.size
				# read in an array of WORD values of length table[:Items] to get the sizes of each summary data entry
				# then increment the pointer appropriately
				size_list = summary_ptr.read_array_of_type(:uint16, :read_uint16, self[:Items])
				summary_ptr += 2 * self[:Items]

				# the values in size_list are 0 if the value is empty and 2 + the size of the value otherwise
				# this 2 comes from the size of the data type, which is for some reason not accounted for
				#    when the value is empty

				column_values = []
				0.upto(self[:Items]-1) do |i|
					if(size_list[i] == 0)
						column_values << nil
						summary_ptr += 2
					else
						# read in usable item types
						column_values << API::read_item_value(summary_ptr, size_list[i], nil)

						summary_ptr += size_list[i]
					end
				end
				@values = column_values
				
				self
			end
		end
		class ITEM_TABLE < FFI::Struct
			layout :Length, :USHORT,
				:Items, :USHORT
			# This is followed by :Items ITEM objects, followed by packed pairs of the item name
			# and item value. Each data value stores its type in the first USHORT
			
			attr_reader :items
			# This convenience method will read in the table's items
			# This shouldn't happen automatically on construction, since Domino
			# may use "stub" versions from time to time, if I recall correctly
			def read_items!
				# Advance to the table's ITEM structures and read them in
				summary_ptr = self.to_ptr + self.size
				item_info = []
				1.upto(self[:Items]) do
					item_info << API::ITEM.new(summary_ptr)
					summary_ptr += API::ITEM.size
				end
				
				# Now that we have the name and data size values for the ITEMS, read them in
				column_items = []
				0.upto(self[:Items]-1) do |i|
					# Read in the name
					name = summary_ptr.get_bytes(0, item_info[i][:NameLength])
					summary_ptr += item_info[i][:NameLength]
					
					# Presumably, this works like SUMMARYVALUES, in that a 0 item size means no data entry
					type = 0
					value = nil
					if item_info[i][:ValueLength] > 0
						value = API::read_item_value(summary_ptr, item_info[i][:ValueLength], nil)
						
						summary_ptr += item_info[i][:ValueLength]
					end
					
					column_items << API::Item.new(name, item_type, value)
					#column_items[name] = value
				end
				@items = column_items
				
				self
			end
			
		end
		class ITEM < FFI::Struct
			layout :NameLength, :USHORT,
				:ValueLength, :USHORT
		end
		class LICENSED < FFI::Struct
			layout :ID, [:BYTE, 5],
				:Product, :BYTE,
				:Check, [:BYTE, 2]
		end
		class NAMES_LIST < FFI::Struct
			layout :NumNames, :WORD,
				:License, :uint64,
				:Authenticated, :int32
			# This is followed by :num_names packed strings
			
			def to_h
				# read in :num_names names, which are null-terminated strings, one after the other in memory
				# offset it by the size of the main structure, which I guess is 16
				offset = self.size
				names = []
				ptr = self.to_ptr
				0.upto(self[:NumNames]-1) do |i|
					string = ""
					counter = 0
					char = ptr.get_bytes(offset, 1)
					while char != "\0" and counter < 200 do
						string << char
						counter = counter + 1
						offset = offset + 1
						char = ptr.get_bytes(offset, 1)
					end
					offset = offset + 1
					names << string
				end
				
				{
					:num_names => self[:NumNames],
					:license => self[:License],
					:authenticated => self[:Authenticated] == 1,
					:names => names
				}
			end
		end
		class LIST < FFI::Struct
			attr_accessor :type
			
			layout :ListEntries, :USHORT
			# This is followed by the list entries
			
			def self.from_unid_array(unids)
				total_size = LIST.size + unids.length * API::UNIVERSALNOTEID.size
				entire_list = FFI::MemoryPointer.new(total_size)
				list = LIST.new(entire_list)
				list.type = :ref
				list[:ListEntries] = unids.length
				
				append_ptr = entire_list + LIST.size
				unids.each do |unid|
					append_ptr.write_uint64 unid[:File]
					append_ptr += 8
					append_ptr.write_uint64 unid[:Note]
					append_ptr += 8
				end
				
				list
			end
			def total_size
				size + self[:ListEntries] * API::UNIVERSALNOTEID.size
			end
		end
		class RANGE < FFI::Struct
			attr_accessor :type
			
			layout :ListEntries, :USHORT,
				:RangeEntries, :USHORT
			
			
			def self.from_number_array(nums)
				
				total_size = RANGE.size + nums.length * API.find_type(:NUMBER).size
				entire_range = FFI::MemoryPointer.new(total_size)
				range = RANGE.new(entire_range)
				range.type = :number
				range[:ListEntries] = nums.length
				range[:RangeEntries] = 0
				
				append_ptr = entire_range + RANGE.size
				nums.each do |val|
					append_ptr.write_double(val)
					append_ptr += API.find_type(:NUMBER).size
				end
				
				range
			end
			def total_size
				size + self[:ListEntries] * API.find_type(:NUMBER).size
			end
		end
		class TIMEDATE < FFI::Struct
			# This isn't meant to be used by humans, hence the super-useful field name
			layout :Innards, [:DWORD, 2]
			
			def self.from_t(t)
				time = TIME.from_t(t)
				TIMEDATE.new(time.to_ptr + API.find_type(:int).size * 10)
			end
			
			def to_time
				API.timedate_to_time(self)
			end
			def to_t
				to_time.to_t
			end
			
			def to_i
				(self[:Innards][0] << 32) + self[:Innards][1]
			end
			
			def to_replicaid
				("%08X" % self[:Innards][1]) + ":" + ("%08X" % self[:Innards][0])
			end
		end
		class TIMEDATE_PAIR < FFI::Struct
			layout :Lower, TIMEDATE,
				:Upper, TIMEDATE
			
			def to_r
				Range.new(API.timedate_to_time(self[:Lower]).to_t, API.timedate_to_time(self[:Upper]).to_t)
			end
			def to_s
				self.to_r.to_s
			end
		end
		class TIME < FFI::Struct
			layout :year, :int,
				:month, :int,
				:day, :int,
				:weekday, :int,
				:hour, :int,
				:minute, :int,
				:second, :int,
				:hundredth, :int,
				:dst, :int,
				:zone, :int,
				:GM, :uint64

			def self.from_timedate(timedate)
				time = TIME.new
				time[:GM] = timedate.to_ptr.read_uint64
				API.TimeGMToLocal(time.to_ptr)
				time
			end
			def self.from_t(t)
				time = TIME.new
				time[:year] = t.year
				time[:month] = t.month
				time[:day] = t.day
				time[:hour] = t.hour
				time[:minute] = t.min
				time[:second] = t.sec
				API.TimeLocalToGM(time.to_ptr)
				time
			end

			# GM is actually a TIMEDATE structure
			def to_t
				if self[:hour] == -1
					Date.new(self[:year], self[:month], self[:day])
				else
					Time.utc(
						self[:year] == -1 ? 0 : self[:year],
						self[:month] == -1 ? 0 : self[:month],
						self[:day] == -1 ? 0 : self[:day],
						self[:hour] == -1 ? 0 : self[:hour],
						self[:minute] == -1 ? 0 : self[:minute],
						self[:second] == -1 ? 0 : self[:second]
					)
				end
			end
			def to_s
				self.to_t.to_s
			end
		end
		class COLLATION < FFI::Struct
			layout :BufferSize, :USHORT,
				:Items, :USHORT,
				:Flags, :BYTE,
				:signature, :BYTE
			# This is followed by :Items COLLATE_DESCRIPTOR objects, then by a string filling the rest of :BufferSize
		end
		class COLLATE_DESCRIPTOR < FFI::Struct
			layout :Flags, :BYTE,
				:signature, :BYTE,
				:keytype, :BYTE,
				:NameOffset, :WORD,
				:NameLength, :WORD
		end
		class COLLECTIONDATA < FFI::Struct
			layout :DocCount, :DWORD,
				:DocTotalSize, :DWORD,
				:BTreeLeafNodes, :DWORD,
				:BTreeDepth, :WORD,
				:Spare, :WORD,
				:KeyOffset, [:DWORD, PERCENTILE_COUNT]
		end
		class COLLECTIONSTATS < FFI::Struct
			layout :TopLevelEntries, :DWORD,
				:LastModifiedTime, :DWORD
		end
		class BLOCKID < FFI::Struct
			layout :pool, :DHANDLE,
				:block, :BLOCK
		end
		class UNIVERSALNOTEID < FFI::Struct
			layout :File, :DBID,
				:Note, :TIMEDATE_S
				
			def self.from_s(unid)
				unid = unid.to_s
				file = unid[0..15].to_i(16)
				note = unid[16..31].to_i(16)
				
				unid_struct = UNIVERSALNOTEID.new
				unid_struct[:File] = file
				unid_struct[:Note] = note
				unid_struct
			end
			
			def to_i
				(self[:File] << 64) + self[:Note]
			end
			def to_s
				"%032X" % self.to_i
			end
		end
		class ORIGINATORID < FFI::Struct
			layout :File, :DBID,
				:Note, :TIMEDATE_S,
				:Sequence, :DWORD,
				:SequenceTime, :TIMEDATE_S
		end
		class MIME_PART < FFI::Struct
			layout :Version, :WORD,
				:Flags, :DWORD,
				:PartType, :BYTE,
				:Spare, :BYTE,
				:ByteCount, :WORD,
				:BoundaryLen, :WORD,
				:HeadersLen, :WORD,
				:Spare, :WORD,
				:Spare, :DWORD
		end
		class DBREPLICAINFO < FFI::Struct
			layout :ID, TIMEDATE,
				:Flags, :WORD,
				:CutoffInterval, :WORD,
				:Cutoff, TIMEDATE
		end
		class COLLATION < FFI::Struct
			layout :BufferSize, :USHORT,
				:Items, :USHORT,
				:Flags, :BYTE,
				:signature, :BYTE
		end
		class COLLATE_DESCRIPTOR < FFI::Struct
			layout :Flags, :BYTE,
				:signature, :BYTE,
				:keytype, :BYTE,
				:NameOffset, :WORD,
				:NameLength, :WORD
			attr_accessor :name
		end
		
		enum :HTMLAPI_PROP_TYPE, [
			:HTMLAPI_PROP_TEXTLENGTH, 0,
			:HTMLAPI_PROP_NUMREFS, 1,
			:HTMLAPI_PROP_USERAGENT_LEN, 2,
			:HTMLAPI_PROP_USERAGENT, 4,
			:HTMLAPI_PROP_BINARYDATA, 6,
			:HTMLAPI_PROP_MIMEMAXLINELENSEEN, 102
		]
		
		# DXL enums
		enum :DXLIMPORTOPTION, [
			:DXLIMPORTOPTION_IGNORE, 1,
			:DXLIMPORTOPTION_CREATE, 2,
			:DXLIMPORTOPTION_IGNORE_ELSE_CREATE, 3,
			:DXLIMPORTOPTION_CREATE_RESERVED2, 4,
			:DXLIMPORTOPTION_REPLACE_ELSE_IGNORE, 5,
			:DXLIMPORTOPTION_REPLACE_ELSE_CREATE, 6,
			:DXLIMPORTOPTION_REPLACE_RESERVED1, 7,
			:DXLIMPORTOPTION_REPLACE_RESERVED2, 8,
			:DXLIMPORTOPTION_UPDATE_ELSE_IGNORE, 9,
			:DXLIMPORTOPTION_UPDATE_ELSE_CREATE, 10,
			:DXLIMPORTOPTION_UPDATE_RESERVED1, 11,
			:DXLIMPORTOPTION_UPDATE_RESERVED2, 12
		]
		enum :DXL_EXPORT_PROPERTY, [
			:eDxlEportResultLog, 1,
			:eDefaultDoctypeSYSTEM, 2,
			:eDoctypeSYSTEM, 3,
			:eDXLBannerComments, 4,
			:eDXLExportCharset, 5,
			:eDxlRichtextOption, 6,
			:eDxlExportResultLogComment, 7,
			:eDxlValidationStyle, 8,
			:eDxlDefaultSchemaLocation, 10,
			:eDxlMimeOption, 11,
			:eAttachmentOmittedExt, 12,
			:eOLEObjectOmittedText, 13,
			:ePictureOmittedText, 14,
			:eOmitItemName, 15,
			:eRestrictToItemNames, 16,
			:eForceNoteFormat, 30,
			:eExitOnFirstFatalError, 31,
			:eOutputRootAttrs, 32,
			:eOutputXmlDecl, 33,
			:eOutputDOCTYPE, 34,
			:eConvertNotesbitmapsToGIF, 35,
			:eOmitRichtextAttachments, 36,
			:eOmitOLEObjects, 37,
			:eOmitMiscFileObjects, 38,
			:eOmitPictures, 39,
			:eUncompressAttachments, 40
		]
	end
end