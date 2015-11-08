Todo
====

Blog
----
* Either update the existing style or make a new one
	* May as well use OneUI class names, whether or not I use the style itself
* Fix comment posting on iOS
	* Could add a Markdown option for all users as well, potentially
* Add web UIs for post/comment/etc. management
* Add email notifications (all new comments for me, "when others comment" for commenters)
* Add a Google AdSense module (?)

Ruby
----
* Figure out why method bindings don't have access to Ruby objects in dataContexts
* Fix some non-serializable Ruby data-type problems
	* String results turn into unserializably-encoded strings for some reason
* Add Ruby script libraries as a design element in Designer (http://frostillic.us/f.nsf/posts/327)
* Get Designer to understand (or at least completely ignore) Ruby value/method bindings
* See about replacing the .runScriptlet() method_missing with a Java implementation (.getProvider().getRuntime().setDefaultMethodMissing()?)
* Ruby compiled classes via Designer - *.rb files translated to *.java via the JRuby compiler in the Eclipse workspace - to be analogous to the Java design element

Domino Classes
--------------
* to_json for Views and Documents

Misc
----
* Improve scripting support for other languages
	* Investigate why Python vomits up NullPointerExceptions after the first page load
* Domino as a Rails app server?