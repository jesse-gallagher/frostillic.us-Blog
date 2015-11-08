module Domino
	class Base
		def initialize(*args)
			@children = []
		end
		def close
			@children.each do |child|
				child.close if child.is_a? Base
			end
			@closed = true
		end
		def closed?; @closed == true; end
		alias_method :recycle, :close
		
		private
		def add_child(child)
			@children << child
			child
		end
	end
end