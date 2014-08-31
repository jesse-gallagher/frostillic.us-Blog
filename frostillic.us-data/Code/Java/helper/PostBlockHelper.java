package helper;

import lotus.domino.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;

import frostillicus.JSFUtil;

public class PostBlockHelper {

	private transient DominoDocument doc;

	public PostBlockHelper(DominoDocument doc) {
		this.doc = doc;
	}

	public int getCommentCount() throws NotesException {
		Database database = ExtLibUtil.getCurrentDatabase();
		View commentsView = database.getView("Comments");
		ViewNavigator nav = commentsView.createViewNavFromCategory((String)doc.getValue("PostID"));
		int count = nav.getCount();
		nav.recycle();
		commentsView.recycle();
		return count;
	}
	public boolean isEditable() throws Exception {
		return JSFUtil.isDocEditableBy(doc.getDocument(), ExtLibUtil.getCurrentSession().getEffectiveUserName());
	}
	public boolean isDraft() throws NotesException {
		return doc.getItemValueString("Status").equals("Draft");
	}

	public String getCommentsString() throws NotesException {
		int commentCount = this.getCommentCount();
		return commentCount == 1 ? "1 comment" : (commentCount + " comments");
	}
}
