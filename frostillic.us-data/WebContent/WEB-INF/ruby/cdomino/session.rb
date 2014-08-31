module Domino
	class Session < Base
		def initialize
			super
			
#			argv = Session.string_array_to_inoutptr([program, ini])
#			
#			result = API.NotesInitExtended(2, argv)
#			raise NotesException.new(result) if result != 0
#			
#			result = API.HTMLProcessInitialize
#			raise NotesException.new(result) if result != 0
		end
		
		def ping(server)
			result = API.NSPingServer(server, nil, nil)
			raise NotesException.new(result) if result != 0
			true
		end
		
		def send_console_command(server, command)
			hResponseText = FFI::MemoryPointer.new(API.find_type(:DHANDLE))
			result = API.NSFRemoteConsole(server, command, hResponseText)
			raise NotesException.new(result) if result != 0
			
			response_ptr = API.OSLockObject(hResponseText.read_uint32)
			response = response_ptr.read_string
			API.OSUnlockObject(hResponseText.read_uint32)
			response
		end
		
		def evaluate(formula, context=nil)
			# First, compile the formula
			rethFormula = FFI::MemoryPointer.new(API.find_type(:FORMULAHANDLE))
			retFormulaLength = FFI::MemoryPointer.new(API.find_type(:WORD))
			retCompileError = FFI::MemoryPointer.new(API.find_type(:STATUS))
			retCompileErrorLine = FFI::MemoryPointer.new(API.find_type(:WORD))
			retCompileErrorColumn = FFI::MemoryPointer.new(API.find_type(:WORD))
			retCompileErrorOffset = FFI::MemoryPointer.new(API.find_type(:WORD))
			retCompileErrorLength = FFI::MemoryPointer.new(API.find_type(:WORD))
			
			result = API.NSFFormulaCompile(
				nil,
				0,
				formula.to_s,
				formula.to_s.size,
				rethFormula,
				retFormulaLength,
				retCompileError,
				retCompileErrorLine,
				retCompileErrorColumn,
				retCompileErrorOffset,
				retCompileErrorLength
			)
			raise NotesException.new(result) if result != 0
			
			compile_error = retCompileError.read_uint16
			if compile_error != 0
				raise Exception.new("Formula compile error: " + {
					:code => compile_error,
					:line => retCompileErrorLine.read_uint16,
					:column => retCompileErrorColumn.read_unt16,
					:offset => retCompileErrorOffset.read_uint16,
					:length => retCompileErrorLength.read_uint16
				}.to_s)
			end
			
			compiled_formula = API.OSLockObject(rethFormula.read_uint32)
			
			# Start the formula environment
			rethCompute = FFI::MemoryPointer.new(API.find_type(:HCOMPUTE))
			result = API.NSFComputeStart(0, compiled_formula, rethCompute)
			raise NotesException.new(result) if result != 0
			
			# Evaluate the formula
			rethResult = FFI::MemoryPointer.new(API.find_type(:DHANDLE))
			retResultLength = FFI::MemoryPointer.new(API.find_type(:WORD))
			retNoteModified = FFI::MemoryPointer.new(API.find_type(:WORD))
			result = API.NSFComputeEvaluate(
				rethCompute.read_pointer,
				context == nil ? 0 : context.handle,
				rethResult,
				retResultLength,
				nil,
				nil,
				retNoteModified
			)
			raise NotesException.new(result) if result != 0
			
			formula_result_ptr = API.OSLockObject(rethResult.read_uint32)
			formula_result = API.read_item_value(formula_result_ptr, retResultLength.read_uint16, nil)
			
			API.OSUnlockObject(rethResult.read_uint32)
			API.OSMemFree(rethResult.read_uint32)
			
			# Close down the formula environment
			result = API.NSFComputeStop(rethCompute.read_pointer)
			raise NotesException.new(result) if result != 0
			
			API.OSUnlockObject(rethFormula.read_uint32)
			
			return formula_result
		end
		
		def username
			username = FFI::MemoryPointer.new(:char, API::MAXUSERNAME+1)
			result = API.SECKFMUserInfo(1, username, nil)
			if result == 0
				username.read_string
			else
				raise NotesException.new(result)
			end
		end
		def user_info
			self.get_user_info(self.username)
		end
		def get_user_info(username)
			names_list_handle = FFI::MemoryPointer.new(:int)
			result = API.NSFBuildNamesList(username, 0, names_list_handle)
			if result == 0
				#API::NameInfo.new(names_list_handle.read_int)
				ptr = API.OSLockObject(names_list_handle.read_int)
				name_list = API::NAMES_LIST.new(ptr)
				hash = name_list.to_h
				API.OSUnlockObject(names_list_handle.read_int)
				hash
			else
				raise NotesException.raise(result)
			end
		end
		
		def get_database(server, path)
			db_handle = FFI::MemoryPointer.new(:int)
			result = API.NSFDbOpen(construct_path(server, path), db_handle)
			if result != 0
				raise NotesException.new(result)
			end
			add_child Database.new(db_handle.read_int)
		end
=begin
		def get_database_as_user(server, path, username)
			names_list = FFI::MemoryPointer.new(:int, API::NAMES_LIST.size + 100)
			result = API.NSFBuildNamesList(username, 0, names_list)
			if result == 0
				names_list_obj = API::NAMES_LIST.new(API.OSLockObject(names_list.read_int))
				names_list_obj[:authenticated] = API::NAMES_LIST_AUTHENTICATED | API::NAMES_LIST_PASSWORD_AUTHENTICATED
				puts names_list_obj[:authenticated]
				
				#API.OSUnockObject(names_list.read_int)
				
				
				db_handle = FFI::MemoryPointer.new(:int)
				result = API.NSFDbOpenExtended(construct_path(server, path), 0, names_list.read_int, nil, db_handle, nil, nil)
				if result != 0
					raise NotesException.new(result)
				end
				Database.new(db_handle.read_int)
			else
				raise NotesException.new(result)
			end
		end
=end
		
		def close
			super
			
#			API.HTMLProcessTerminate
#			API.NotesTerm
		end
		
		def construct_path(server, path)
			if server == nil or server == ""
				path
			else
				"#{server}!!#{path}"
			end
		end
		
		
		
		def self.html_converter(options=nil)
			if options == nil or !options.is_a? Array
				options = [
					"ForceSectionExpand=1",
					"ForceOutlineExpand=1",
					"RowAtATimeTableAlt=1",
					"TableCaptionFromTitle=1",
					"TextExactSpacing=1",
					"XMLCompatibleHTML=1",
					nil
				]
			else
				options << nil if options.last != nil
			end
			
			htmlhandle_ptr = FFI::MemoryPointer.new(API.find_type(:HTMLHANDLE))
			
			# Initialize the HTML converter
			result = API.HTMLCreateConverter(htmlhandle_ptr)
			raise NotesException.new(result) if result != 0
			htmlhandle = htmlhandle_ptr.read_uint32
			
			html = ""
			
			# Set the converter options
			options_ptr = Domino::Session.string_array_to_inoutptr(options)
			result = API.HTMLSetHTMLOptions(htmlhandle, options_ptr)
			raise NotesException.new(result) if result != 0
			
			# This converts the item into the converter's special buffer
			#result = API.HTMLConvertItem(htmlhandle, @parent.handle, @handle, item_name.to_s)
			yield htmlhandle
			
			# To get the buffer content, find the text length, create a buffer, and fetch
			text_length = FFI::MemoryPointer.new(API.find_type(:DWORD))
			result = API.HTMLGetProperty(htmlhandle, :HTMLAPI_PROP_TEXTLENGTH, text_length)
			raise NotesException.new(result) if result != 0
			
			text = FFI::MemoryPointer.new(:char, text_length.read_uint32)
			result = API.HTMLGetText(htmlhandle, 0, text_length, text)
			raise NotesException.new(result) if result != 0
			
			html = text.read_string(text_length.read_uint32)
			
			# Destroy the HTML converter
			result = API.HTMLDestroyConverter(htmlhandle)
			raise NotesException.new(result) if result != 0
			
			html
		end
		
		def self.string_array_to_inoutptr(ary)
			ptrs = ary.map { |a| a == nil ? nil : FFI::MemoryPointer.from_string(a) }
			block = FFI::MemoryPointer.new(:pointer, ptrs.length)
			block.write_array_of_pointer ptrs
			#argv = FFI::MemoryPointer.new(:pointer)
			#argv.write_pointer block
			#argv
			block
		end
		def int_to_inoutptr(val)
			ptr = FFI::MemoryPointer.new(:int)
			ptr.write_int val
			ptr
		end
	end
  
end