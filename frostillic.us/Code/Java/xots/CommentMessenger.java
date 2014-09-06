package xots;

import model.Comment;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.events.IDominoEvent;
import org.openntf.domino.thread.DominoSessionType;
import org.openntf.domino.xots.XotsAbstractTriggeredTasklet;
import org.openntf.domino.xots.annotations.Persistent;
import org.openntf.domino.xots.annotations.Trigger;

@Trigger("newBlogComment")
@Persistent
public class CommentMessenger extends XotsAbstractTriggeredTasklet {
	private static final long serialVersionUID = 1L;

	boolean sent_ = false;

	@Override
	public DominoSessionType getSessionType() {
		return DominoSessionType.NATIVE;
	}

	public void handleEvent(final IDominoEvent event) {
		if(event.getPayload() instanceof Comment) {
			Comment comment = (Comment)event.getPayload();
			Document commentDoc = comment.document();
			Database database = commentDoc.getAncestorDatabase();

			Document mailDoc = database.createDocument();
			mailDoc.replaceItemValue("Form", "Memo");
			// TODO move to config
			mailDoc.replaceItemValue("SendTo", "Jesse Gallagher/Frost");
			mailDoc.replaceItemValue("Subject", "New comment on " + database.getTitle());
			// TODO make adapt to server setup
			mailDoc.replaceItemValue("Body", "http://frostillic.us/f.nsf/posts/" + commentDoc.getItemValueString("PostID"));
			mailDoc.send();
		}

		sent_ = true;
	}

	@Override
	public boolean shouldStop() {
		return sent_;
	}
}
