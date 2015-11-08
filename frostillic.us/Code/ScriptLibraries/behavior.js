var DojoBehavior = {
	"div.lotusMain": {
		// Tag the content are with some extra classes if there are left and/or right columns
		found: function(div) {
			var leftCol = dojo.query("div.lotusColLeft").length > 0
			var rightCol = dojo.query("div.lotusColRight").length > 0
			if(leftCol) { dojo.addClass(div, "hasLeftCol") }
			if(rightCol) { dojo.addClass(div, "hasRightCol") }
			if(leftCol && rightCol) { dojo.addClass(div, "hasBothCols") }
		}
	},
	"#linksbar div.dijitContentPane": {
		found: function(div) {
			if(div.title == "") div.title = null
		}
	},
	"body": function() {
		SyntaxHighlighter.all()
	},
	"#content": function(div) {
		if(div.offsetHeight < dojo.byId("linksbar").offsetHeight) {
			div.style.minHeight = dojo.byId("linksbar").offsetHeight + "px"
		}
	}
}

XSP.addOnLoad(function() {
	dojo.behavior.add(DojoBehavior)
	dojo.behavior.apply()
})