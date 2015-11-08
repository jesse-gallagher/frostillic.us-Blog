require "ffi"

#require "#{File.dirname(__FILE__)}/domino/base"
#require "#{File.dirname(__FILE__)}/domino/api"
#require "#{File.dirname(__FILE__)}/domino/session"
#require "#{File.dirname(__FILE__)}/domino/database"
#require "#{File.dirname(__FILE__)}/domino/document"
#require "#{File.dirname(__FILE__)}/domino/viewentry"
#require "#{File.dirname(__FILE__)}/domino/viewentrycollection"
#require "#{File.dirname(__FILE__)}/domino/view"
#require "#{File.dirname(__FILE__)}/domino/form"
#require "#{File.dirname(__FILE__)}/domino/documentcollection"
#require "#{File.dirname(__FILE__)}/domino/item"

module Domino
	NotesErrors = {
		259 => "File does not exist",
		273 => "Unable to access files directory",
		421 => "The NOTES.INI file cannot be found on the search path (PATH)",
		546 => "Note item not found",
		582 => "You are not authorized to perform that operation",
		781 => "You are not authorized to access the view",
		813 => "Collation number specified negative or greater than number of collations in view.",
		947 => "Attempt to perform folder operation on non-folder note.",
		1028 => "Entry not found in index",
		1387 => "Internal error:  Corrupted formula instance detected",
		1543 => "Encountered zero length record.",
		2055 => "The server is not responding. The server may be down or you may be experiencing network problems. Contact your system administrator if this problem persists.",
		2232 => "Warning: unexpected MIME error: ",
		8459 => "An attempt was made to load a program with an incorrect format.",
		14938 => "Feature not supported in the HTML API.",
		14941 => "HTMLAPI Problem converting to HTML",
		15097 => "No MIME data."
	}
	
	module LibC
		extend FFI::Library
		ffi_lib FFI::Library::LIBC
		
		attach_function :malloc, [:size_t], :pointer
		attach_function :free, [:pointer], :void
		
		attach_function :memcpy, [:pointer, :pointer, :size_t], :pointer
	end
	
	class NotesException < Exception
		def initialize(status)
			@error_code = status & API::ERR_MASK
		end
		def message
			return API.error_string(@error_code)
			
			
=begin
			#puts @error_code
			begin
				#ruby_buffer = FFI::MemoryPointer.new(256).write_string("\0" * 256)
				#buffer = LibC.malloc 256
				#buffer.write_array_of_type(:uint8, :write_uint8, [0] * 256)
				#size = API.OSLoadString(API::NULLHANDLE, 3847, buffer, 255)
				buff = FFI::MemoryPointer.new(256)
				size = API.OSLoadString(API::NULLHANDLE, @error_code, buff, 255)
				#mess = buffer.read_bytes(size)
				#LibC.free buffer
				#puts "Excep: #{mess}"
				puts buff.get_bytes(20)
			rescue Exception => e
				puts "Couldn't read message: #{e}"
			end
			#API.error_string(@error_code)
=end
		end
	end
end