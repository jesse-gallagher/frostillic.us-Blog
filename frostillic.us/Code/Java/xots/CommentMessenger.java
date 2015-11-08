package xots;

import model.Comment;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.xots.Tasklet;

@Tasklet(session=Tasklet.Session.NATIVE)
public class CommentMessenger implements Runnable {
	private static final long serialVersionUID = 1L;

	private Comment comment_;

	public CommentMessenger(final Comment comment) {
		comment_ = comment;
	}

	public void run() {
		Document commentDoc = comment_.document();
		Database database = commentDoc.getAncestorDatabase();

		Document mailDoc = database.createDocument();
		mailDoc.replaceItemValue("Form", "Memo"); //$NON-NLS-1$ //$NON-NLS-2$
		// TODO move to config
		mailDoc.replaceItemValue("SendTo", "Jesse Gallagher/Frost");  //$NON-NLS-1$//$NON-NLS-2$
		mailDoc.replaceItemValue("Subject", "New comment on " + database.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
		// TODO make adapt to server setup
		mailDoc.replaceItemValue("Body", "http://frostillic.us/f.nsf/posts/" + commentDoc.getItemValueString("PostID")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		mailDoc.send();
	}
}
