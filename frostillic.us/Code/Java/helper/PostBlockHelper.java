package helper;

import org.openntf.domino.*;
import org.openntf.domino.utils.XSPUtil;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;

import frostillicus.JSFUtil;

public class PostBlockHelper {

	private transient DominoDocument doc;

	public PostBlockHelper(DominoDocument doc) {
		this.doc = doc;
	}

	public int getCommentCount() {
		Database database;
		try {
			database = XSPUtil.getCurrentDatabase();
			View commentsView = database.getView("Comments");
			ViewNavigator nav = commentsView.createViewNavFromCategory((String)doc.getValue("PostID"));
			int count = nav.getCount();
			return count;
		} catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	public boolean isEditable() throws Exception {
		return JSFUtil.isDocEditableBy(XSPUtil.wrap(doc.getDocument()), XSPUtil.getCurrentSession().getEffectiveUserName());
	}
	public boolean isDraft() {
		return doc.getValue("Status").equals("Draft");
	}

	public String getCommentsString() {
		int commentCount = this.getCommentCount();
		return commentCount == 1 ? "1 comment" : (commentCount + " comments");
	}
}
