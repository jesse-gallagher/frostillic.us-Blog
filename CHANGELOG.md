CHANGELOG
=========

2012/5/31
---------
* Blog
	* Replaced the scheduled agent that periodically fecthed GitHub repo data with a <xe:restJsonService/> data source
	* Added SyntaxHighligher
* Java/JRuby
	* Ruby "Script Libraries" are now compiled and stored in the application scope on first request. That compiled form is then used on all subsequent requests, which should theoretically provide a performance boost

2012/5/28
---------
* Blog
	* Added a "Thread" field to posts and a list of links in threaded posts

2012/5/6
--------
* Java/JRuby
	* Added a class frostillicus.ruby.RubyPhaseListener to terminate the Ruby runtime if it was instantiated for the current request
* Blog
	* Changed some Ruby code to use more API extensions for cleaner code

2012/4/30
---------
* Java/JRuby
	* Switched back to using ScriptingContainer for Ruby scripting for classloading purposes
	* Added numerous methods to the domino.rb library:
		* Array-like access to ViewEntryCollections and DocumentCollections
		* Hash-like access to Documents
		* Document#user_editable? to determine whether the current user can edit the document
		* Database#query_access_names to get a names list for the given user (like @UserNamesList)
		* View#document and Form#document to get Document representations of the design note
		* Form#universal_id to get the Form document's UNID
		* Form#generateXML and View#generateXML to get the design note's DXL

2012/4/19
---------
* Java/JRuby
	* Added a "domino.rb" Ruby script library with some convenience methods for Documents and the collection classes
* Blog
	* Fixed the Google Analytics control, which apparently never worked in the first place
	* Switched from <link> tags to <meta> for JS-used "base" and "search" hints

2012/4/18
---------
* Java/JRuby
	* Switched the JRuby runtime context mode from "concurrent" to "threadsafe". Threadsafe mode is slower, but concurrent mode put all "local" method declarations in the global runtime eternally, which was no good
	* Improved "script library" support to take into account any resources added after initial page load, such as those in Themes or added via code

2012/4/13
---------
* Java/JRuby
	* Changed the library-importing code to only include a given library once (by name)
	* Switched to using the JSR 223 javax.script.* version of JRuby embedding
	* Added frostillicus.GenericBindingFactory to provide limited support for other available JSR 223 languages
* Blog
	* Added a reader field to Posts to enforce the Draft/Published setting

2012/4/11
---------
* Project
	* Added CHANGELOG
* Java/JRuby
	* Removed classes out of the mtc.* and mcl.* packages
	* Added license headers to the JRuby plugin files
* Blog
	* Changed the "remember me" checkbox to check itself when you have applicable cookies 