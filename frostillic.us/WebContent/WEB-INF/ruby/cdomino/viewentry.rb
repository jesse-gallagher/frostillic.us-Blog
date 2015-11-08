module Domino
	class ViewEntry < Base
		attr_reader :index, :noteid, :unid, :note_class
		attr_reader :sibling_count, :child_count, :descendant_count, :indent_level
		attr_reader :any_unread, :unread, :position, :ft_search_score
		attr_reader :column_items
		
		def column_values
			if @parent.read_column_names
				@column_items.map { |item| item.value }
			else
				@column_values
			end
		end
		
		def read(parent, index, info_ptr, read_mask)
			@parent = parent
			@view = @parent.is_a?(View) ? @parent : @parent.parent
			@index = index
			
			# Read in the NOTEID and advance past it
			if read_mask & API::READ_MASK_NOTEID > 0
				@noteid = info_ptr.read_int
				info_ptr += 4
			end
			
			# Read in the UNIVERSALNOTEID and advance past it
			if read_mask & API::READ_MASK_NOTEUNID > 0
				# No need to store the UNID in its raw form - the components are meaningless now
				@unid = API::UNIVERSALNOTEID.new(info_ptr).to_s
				info_ptr += API::UNIVERSALNOTEID.size
			end
			
			# Note class, a WORD
			if read_mask & API::READ_MASK_NOTECLASS > 0
				@note_class = info_ptr.read_uint16
				info_ptr += 2
			end
			
			# Siblings, a DWORD
			if read_mask & API::READ_MASK_INDEXSIBLINGS > 0
				@sibling_count = info_ptr.read_uint32
				info_ptr += 4
			end
			
			# Children, a DWORD
			if read_mask & API::READ_MASK_INDEXCHILDREN > 0
				@child_count = info_ptr.read_uint32
				info_ptr += 4
			end
			
			# Descendants, a DWORD
			if read_mask & API::READ_MASK_INDEXDESCENDANTS > 0
				@descendant_count = info_ptr.read_uint32
				info_ptr += 4
			end
			
			# Any Unread, a WORD
			if read_mask & API::READ_MASK_INDEXANYUNREAD > 0
				@any_unread = info_ptr.read_uint16 == 1
				info_ptr += 2
			end
			
			# Indent level, a WORD
			if read_mask & API::READ_MASK_INDENTLEVELS > 0
				@indent_level = info_ptr.read_uint16
				info_ptr += 2
			end
			
			# Search score, if the parent was FTSearch'd
			if read_mask & API::READ_MASK_SCORE > 0
				@ft_search_score = info_ptr.read_uint16
				info_ptr += 2
			else
				@ft_search_score = 0
			end
			
			# Unread, a WORD
			if read_mask & API::READ_MASK_INDEXUNREAD > 0
				@unread = info_ptr.read_uint16 == 1
				info_ptr += 2
			end
			
			# Collection Position, which is truncated (the Tumbler array at the end is only as large as need be)
			if read_mask & API::READ_MASK_INDEXPOSITION > 0
				@position = API.read_truncated_collectionposition(info_ptr)
				info_ptr += API.read_truncated_collectionposition_size(info_ptr)
			end
			
			if read_mask & API::READ_MASK_SUMMARYVALUES > 0
				table = API::ITEM_VALUE_TABLE.new(info_ptr).read_values!
				@column_values = table.values
				
				info_ptr += table[:Length]
			end
			
			if read_mask & API::READ_MASK_SUMMARY > 0
				
				table = API::ITEM_TABLE.new(info_ptr).read_items!
				@column_items = table.items
				
				info_ptr += table[:Length]
			end
			
			# Return the final pointer
			info_ptr
		end
		
		def document
			category? ? null : @view.parent.doc_by_id(noteid)
		end
		
		def category?
			noteid & API::NOTEID_CATEGORY > 0
		end
	end
end