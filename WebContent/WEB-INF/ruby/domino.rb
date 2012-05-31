# It's important to instantiate the Java class first so that the Ruby code is extending the
#	existing class rather than making a new class of the same name
import "lotus.domino.local.View"
import "lotus.domino.local.DocumentCollection"
import "lotus.domino.local.ViewEntry"
import "lotus.domino.local.ViewEntryCollection"
import "lotus.domino.local.ViewNavigator"
import "lotus.domino.local.Document"
import "lotus.domino.local.Form"
import "com.ibm.domino.services.util.JsonWriter"
import "com.ibm.xsp.model.domino.wrapped.DominoDocument"
include_class "lotus.domino.local.Item"
include_class "lotus.domino.ACL"
include_class "lotus.notes.addins.DominoServer"
#import "com.ibm.commons.util.io.json.JsonJavaObject"

require "java"

module Java
	module LotusDominoLocal
		
		# Extend the View class for each/collect methods for documents and entries
		class View
			
			# Loop through each Document, with auto-recycling
			def each_doc
				doc = self.get_first_document
				while doc != nil
					yield doc
					
					temp = doc
					doc = self.get_next_document(doc)
					temp.recycle
				end
				
				self
			end
			# Loop through each Document, with auto-recycling, and return an array of the resultant values
			def collect_docs
				result = []
				self.each_doc do |doc|
					result << (yield doc)
				end
				result
			end
			
			# Loop through each ViewEntry, with auto-recycling
			def each_entry
				self.create_view_nav.each { |entry| yield entry }
				self
			end
			# Loop through each ViewEntry, with auto-recycling, and return an array of the resultant values
			def collect_entries
				self.create_view_nav.collect { |entry| yield entry }
			end
			
			# Get a Document object representing the View's design note
			def document; parent.get_document_by_unid(universal_id); end
			
			# Generate a DXL representation of the View's design note
			def generateXML
				doc = document
				xml = document.generate_xml
				doc.recycle
				xml
			end
			alias :generate_xml :generateXML
			alias :to_xml :generateXML
			alias :to_dxl :to_xml
			
			# Generate a JSON representation of the ViewEntries
			def to_json; "[#{self.collect_entries { |entry| entry.to_json }.join(',')}]"; end
		end
		
		# Extend DocumentCollections, ViewEntryCollections, and ViewNavigators for each/collect and set/array operations
		class DocumentCollection
			# Loop through each Document, with auto-recycling
			def each
				doc = self.get_first_document
				while doc != nil
					yield doc
					temp = doc
					doc = self.get_next_document(doc)
					temp.recycle
				end
				
				self
			end
			# Loop through each Document, with auto-recycling, and return an array of the resultant values
			def collect
				result = []
				self.each do |doc|
					result << (yield doc)
				end
				result
			end
			# Loop through each index
			def each_index
				0.upto(self.size - 1) { |i| yield i }
				self
			end
			
			# Create aliases/implementations for the set and array operations
			
			# Same as DocumentCollection#merge
			def +(other); self.merge(other); end
			# Same as DocumentCollection#merge
			def |(other); self.merge(other); end
			# Same as DocumentCollection#subtract
			def -(other); self.subtract(other); end
			# Same as DocumentCollection#intersect
			def &(other); self.intersect(other); end
			
			# Same as DocumentCollection#addDocument
			def push(doc)
				add_document doc
			end
			# Returns the last Document and removes it from the collection
			def pop
				doc = last
				delete doc
				doc
			end
			# Returns the first Document and removes it from the collection
			def shift
				doc = first
				delete doc
				doc
			end
			
			alias :concat :merge
			# Same as DocumentCollection#addDocument
			def <<(doc); add_document doc; end
			alias :size :count
			alias :length :count
			alias :nitems :count
			# Returns true if empty
			def empty?; self.size == 0; end
			alias :delete :delete_document
			# Returns the first Document (like getFirstDocument)
			def first; self[0]; end
			# Returns the last Document (like getLastDocument)
			def last; self[size-1]; end
			
			# Removes the Document at the specified index from the collection
			def delete_at(index)
				delete self[index]
			end
			# Removes Documents from the collection for which the provided block returns true
			def delete_if
				deleter = []
				self.each_index do |i|
					deleter << i if (yield self[i])
				end
				deleter.each do |i|
					delete_at i
				end
			end
			
			alias :include? :contains 
			
			# Returns the index of the first instance of the provided Document
			def index(test_doc)
				i = 0
				self.each do |doc|
					if doc.notesURL == test_doc.notesURL
						doc.recycle
						return i + 1
					end
					i += 1
				end
				nil
			end
			
			# Like getNthDocument, but allows for negative indexes
			def at(index)
				self.get_nth_document(index < 0 ? self.size + index : index)
			end
			# Like getNthDocument, but allows for negative indexes
			def [](index); self.at(index); end
			
			# Returns a Ruby-array representation of the DocumentCollection
			def to_a
				arr = []
				doc = get_first_document
				while doc != nil
					arr << doc
					doc = get_next_document
				end
				arr
			end
			alias :to_ary :to_a
			
			# Generate a JSON representation of the Documents
			def to_json; "[#{self.collect { |doc| doc.to_json }.join(',')}]"; end
		end
		class ViewEntryCollection
			# Loop through each ViewEntry, with auto-recycling
			def each
				entry = self.get_first_entry
				while entry != nil
					yield entry
					temp = entry
					entry = self.get_next_entry(entry)
					temp.recycle
				end
				self
			end
			# Loop through each ViewEntry, with auto-recycling, and return an array of the resultant values
			def collect
				result = []
				self.each do |entry|
					result << (yield entry)
				end
				result
			end
			# Loop through each index
			def each_index
				0.upto(self.size - 1) { |i| yield i }
				self
			end
			
			# Create aliases/implementations for the set and array operations
			
			# Same as ViewEntryCollection#merge
			def +(other); self.merge(other); end
			# Same as ViewEntryCollection#merge
			def |(other); self.merge(other); end
			# Same as ViewEntryCollection#subtract
			def -(other); self.subtract(other); end
			# Same as ViewEntryCollection#intersect
			def &(other); self.intersect(other); end
			
			# Same as ViewEntryCollection#addEntry
			def push(entry)
				add_entry entry
			end
			# Returns the last ViewEntry and removes it from the collection
			def pop
				entry = last
				delete entry
				entry
			end
			# Returns the first Document and removes it from the collection
			def shift
				entry = first
				delete entry
				entry
			end
			
			alias :concat :merge
			# Same as ViewEntryCollection#addEntry
			def <<(entry); add_entry entry; end
			alias :size :count
			alias :length :count
			alias :nitems :count
			# Returns true if empty
			def empty?; self.size == 0; end
			alias :delete :delete_entry
			# Returns the first ViewEntry (like getFirstEntry)
			def first; self[0]; end
			# Returns the last ViewEntry (like getLastEntry)
			def last; self[size-1]; end
			

			# Removes the ViewEntry at the specified index from the collection
			def delete_at(index)
				delete self[index]
			end

			# Removes ViewEntry from the collection for which the provided block returns true
			def delete_if
				deleter = []
				self.each_index do |i|
					deleter << i if (yield self[i])
				end
				deleter.each do |i|
					delete_at i
				end
			end
			
			alias :include? :contains

			# Returns the index of the first instance of the provided ViewEntry
			def index(test_entry)
				i = 0
				self.each do |doc|
					if entry == entry
						entry.recycle
						return i + 1
					end
					i += 1
				end
				nil
			end
	
			# Like getNthEntry, but allows for negative indexes
			def at(index)
				self.get_nth_entry(index < 0 ? self.size + index : index)
			end
			# Like getNthEntry, but allows for negative indexes
			def [](index); self.at(index); end
			
	
			# Returns a Ruby-array representation of the ViewEntryCollection
			def to_a
				arr = []
				entry = get_first_entry
				while entry != nil
					arr << entry
					doc = get_next_entry
				end
				arr
			end
			alias :to_ary :to_a
			
	
			# Generate a JSON representation of the ViewEntries
			def to_json; "[#{self.collect { |entry| entry.to_json }.join(',')}]"; end
		end
		class ViewNavigator
			def each
				entry = self.get_first
				while entry != nil
					yield entry
					temp = entry
					entry = self.get_next(entry)
					temp.recycle
				end
				self
			end
			def collect
				result = []
				self.each do |entry|
					result << (yield entry)
				end
				result
			end
		end
		
		class Database
			# Like @UserNamesList
			def queryNamesList(user)
				server = DominoServer.new(server)
				names = server.get_names_list(user)
				names + query_access_roles(user)
			end
			alias :query_names_list :queryNamesList
		end
		
		# Extend documents for hash-style item access
		class Document
			# Same as getItemValue
			def [](field)
				self.get_item_value(field.to_s)
			end
			# Same as replaceItemValue
			def []=(field, newval)
				self.replace_item_value(field.to_s, newval)
				newval
			end
			
			# Returns true if the current user is able to edit the document (i.e. if they're an Editor in the ACL or in an Author field)
			def user_editable?
				facesContext = javax.faces.context.FacesContext.current_instance
				context = facesContext.application.variable_resolver.resolve_variable(facesContext, "context")
				editable_by? context.user.name
			end
			
			# Returns true if the specified user is able to edit the document (i.e. if they're an Editor in the ACL or in an Author field)
			def editable_by?(user)
				facesContext = javax.faces.context.FacesContext.current_instance
				session = facesContext.application.variable_resolver.resolve_variable(facesContext, "session")
				database = facesContext.application.variable_resolver.resolve_variable(facesContext, "database")
				context = facesContext.application.variable_resolver.resolve_variable(facesContext, "context")
				
				level = database.query_access(user)
				if level >= ACL.LEVEL_EDITOR
					return true
				elsif level < ACL.LEVEL_READER
					return false
				end
				
				server = DominoServer.new(database.server)
				names = server.get_names_list(user)
				names = (names + database.query_access_roles(user)).to_a.map { |name| name.downcase }
				
				items.each do |item|
					if item.type == Item::AUTHORS and (item.values.to_a.map { |name| name.downcase } & names).size > 0
						item.recycle
						return true
					end
					item.recycle
				end
				false
			end
			
			# If an Item with the specified name is in the Document, this removes it and returns its value;
			#	otherwise, it returns nil and optionally executes a provided block with the field name
			def delete(field)
				if has_item(field.to_s)
					val = self[field.to_s]
					remove_item field.to_s
					return val
				else
					if block_given?
						yield field
					end
					return nil
				end
			end
			# Removes Items for which the provided block returns true
			def delete_if
				items.each do |item|
					if (yield item.name, item)
						remove_item item.name
					end
					item.recycle
				end
				self
			end
			
			# Loop through each Item, with auto-recycling
			def each
				items.each do |item|
					yield item.name, item
					item.recycle
				end
				self
			end
			alias :each_pair :each
			# Loop through each Item's name
			def each_key
				items.each do |item|
					yield item.name
					item.recycle
				end
				self
			end
			# Loop through each Item, with auto-recycling
			def each_value
				items.each do |item|
					yield item
					item.recycle
				end
				self
			end
			# Returns false
			def empty?; false; end
			
			alias :has_key? :has_item
			alias :include? :has_key?
			alias :key? :has_key?
			
			# Returns an array of all Item names
			def keys
				result = []
				each_key do |key|
					result << key
				end
				result
			end
			
			alias :to_xml :generateXML
			alias :to_dxl :to_xml
			
			# Returns a JSON representation of the Document
			def to_json
				# Let's try using the JSON classes IBM provides
				baos = java.io.ByteArrayOutputStream.new
				writer = java.io.OutputStreamWriter.new(baos)
				
				jwriter = com.ibm.domino.services.util.JsonWriter.new(writer, false)

				jwriter.start_object
				each do |name, item|
					jwriter.start_property name
					
					# write out the item based on its type
					case item.type
					when Item::NUMBERS then
						vals = item.values
						if vals == nil
							jwriter.out_null
						elsif vals.length == 1
							jwriter.out_number_literal vals.first
						else
							jwriter.start_array
							vals.each do |val|
								jwriter.start_array_item
								jwriter.out_number_literal val
								jwriter.end_array_item
							end
							jwriter.end_array
						end
					when Item::READERS, Item::NAMES, Item::AUTHORS, Item::TEXT then
						vals = item.values
						if vals == nil
							jwriter.out_string_literal ""
						elsif vals.length == 1
							jwriter.out_string_literal vals.first
						else
							jwriter.start_array
							vals.each do |val|
								jwriter.start_array_item
								jwriter.out_string_literal val
								jwriter.end_array_item
							end
							jwriter.end_array
						end
					when Item::DATETIMES then
						vals = item.values
						if vals == nil
							jwriter.out_null
						elsif vals.length == 1
							jwriter.out_domino_value vals.first
						else
							jwriter.start_array
							vals.each do |val|
								jwriter.start_array_item
								jwriter.out_domino_value val
								jwriter.end_array_item
							end
							jwriter.end_array
						end
					else
						jwriter.out_string_literal item.text
					end
					
					jwriter.end_property
				end
				jwriter.end_object
				jwriter.close
				
				baos.to_s
			end
		end
		
		class Form
			# Returns the Universal ID of the Form's design note
			def getUniversalID
				url = notes_url
				url[(url.rindex("/")+1)..(url.rindex("?")-1)]
			end
			alias :universal_id :getUniversalID
			
			# Generate a DXL representation of the Form's design note
			def generateXML
				doc = document
				xml = doc.generate_xml
				doc.recycle
				xml
			end
			alias :generate_xml :generateXML
			alias :to_xml :generateXML
			alias :to_dxl :to_xml
			
			# Get a Document object representing the Form's design note
			def document; parent.get_document_by_unid(universal_id); end
		end
		
		class ViewEntry
			# Returns a JSON representation of the ViewEntry
			def to_json
				baos = java.io.ByteArrayOutputStream.new
				writer = java.io.OutputStreamWriter.new(baos)
				
				w = com.ibm.domino.services.util.JsonWriter.new(writer, false)

				w.start_object
				w.property("@entryid") { w.out_string_literal "#{get_position("."[0])}-#{universal_id}" }
				w.property("@unid") { w.out_string_literal universal_id }
				
				w.end_object
				w.close
				
				baos.to_s
			end
		end
	end
	
	module ComIbmXspModelDominoWrapped
		class DominoDocument
			# Same as getItemValue
			def [](field)
				self.get_item_value(field.to_s)
			end
			# Same as replaceItemValue
			def []=(field, newval)
				self.replace_item_value(field.to_s, newval)
				newval
			end
		end
	end
	
	module ComIbmDominoServicesUtil
		class JsonWriter
			def property(name)
				start_property name
				yield self
				end_property
			end
		end
	end
end