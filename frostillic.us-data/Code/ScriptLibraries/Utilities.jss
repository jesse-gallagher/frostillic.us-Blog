function @WebDbName(db) {
	db = db == null ? database : db
	return java.net.URLEncoder.encode(db.getFilePath().replace("\\", "/"), "UTF-8")
}

var Utilities = {}

Utilities.toArray = function(value) {
	if(typeof value == "undefined") { return [] }
	if(value.length == null || value.shift == null) {
		return [value]
	}
	return value
}
Utilities.replaceText = function(rtitem, replaceText, withText) {
	if(withText == "") { withText = " " }
	var nav = rtitem.createNavigator()
	var range = rtitem.createRange()
	nav.findFirstElement(NotesRichTextItem.RTELEM_TYPE_TEXTPARAGRAPH)
	range.setBegin(nav)
	range.findandReplace(replaceText, withText, NotesRichTextItem.RT_FIND_CASEINSENSITIVE + NotesRichTextItem.RT_REPL_ALL)
}