module Domino
	class DocumentCollection < Base
		attr_reader :parent, :handle
		
		def initialize(parent, handle)
			super
			
			@parent = parent
			@handle = handle
		end
		
		def size
			API.IDEntries(@handle)
		end
		
		def close
			if not self.closed?
				super
				result = API.IDDestroyTable @handle
				raise NotesException.new(result) if result != 0
			end
		end
	end
end