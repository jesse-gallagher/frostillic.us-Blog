module Domino
	class Item < Base
		attr_reader :name, :type, :value
		
		def initialize(parent, name, type, value)
			super
			
			@parent = parent
			@name = name
			@type = type
			@value = value
		end
		
		def to_html
			if @type == API::TYPE_MIME_PART or @type == API::TYPE_COMPOSITE
				@parent.get_item_html(@name)
			else
				@value.to_s
			end
		end
		def to_dxl
			@parent.get_item_dxl(@name)
		end
		def to_h
			{
				:name => @name,
				:type => @type,
				:value => @value
			}
		end
		
		def to_s
			to_h.to_s
		end
	end
end