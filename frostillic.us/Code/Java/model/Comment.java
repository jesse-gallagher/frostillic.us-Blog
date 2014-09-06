package model;

import java.io.IOException;
import java.util.Date;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.openntf.domino.*;
import org.openntf.domino.thread.DominoSessionType;
import org.openntf.domino.xots.XotsBaseTasklet;
import org.openntf.domino.xots.XotsDaemon;

import com.ibm.xsp.model.domino.wrapped.DominoRichTextItem;

import config.AppConfig;

import bean.Akismet;
import frostillicus.xsp.bean.ApplicationScoped;
import frostillicus.xsp.bean.ManagedBean;
import frostillicus.xsp.model.domino.AbstractDominoManager;
import frostillicus.xsp.model.domino.AbstractDominoModel;
import frostillicus.xsp.util.FrameworkUtils;

public class Comment extends AbstractDominoModel {
	private static final long serialVersionUID = 1L;

	@NotEmpty String authorName;
	@NotEmpty @Email String authorEmailAddress;
	String authorURL;

	@Override
	public void initFromDatabase(final Database database) {
		super.initFromDatabase(database);

		setValue("Form", "Comment");
		Post post = (Post)FrameworkUtils.resolveVariable("post");
		if(post != null) {
			setValue("PostID", post.getValue("PostID"));
		}
	}

	@Override
	protected boolean querySave() {
		if(isNew()) {
			setValue("Posted", new Date());
			setValue("CommentID", document().getUniversalID());

			HttpServletRequest req = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String remoteAddr = req.getRemoteAddr();
			String userAgent = req.getHeader("User-Agent");
			String referrer = req.getHeader("Referer");
			setValue("HTTP_Referer", referrer);
			setValue("HTTP_User_Agent", userAgent);
			setValue("Remote_Addr", remoteAddr);

			Akismet akismet = Akismet.get();
			AppConfig appConfig = AppConfig.get();
			akismet.setApiKey((String)appConfig.getValue("akismetAPIKey"));
			akismet.setBlog((String)appConfig.getValue("akismetBlog"));

			DominoRichTextItem body = (DominoRichTextItem)getValue("body");
			try {
				boolean spam = akismet.checkComment(remoteAddr, userAgent, referrer, "", "comment", (String)getValue("authorName"), (String)getValue("authorEmailAddress"), (String)getValue("authorURL"), body.getHTML());
				setValue("AkismetSpam", spam ? 1 : 0);
			} catch(IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		setValue("FullName", getValue("authorName"));
		return super.querySave();
	}

	@Override
	protected void postSave() {
		XotsDaemon.addToQueue(new CommentEmailer(this));
	}

	public static class CommentEmailer extends XotsBaseTasklet {
		private static final long serialVersionUID = 1L;

		private final String databaseApiPath_;
		private final String commentId_;

		public CommentEmailer(final Comment comment) {
			Document doc = comment.document();
			databaseApiPath_ = doc.getAncestorDatabase().getApiPath();
			commentId_ = doc.getUniversalID();
		}

		@Override
		public DominoSessionType getSessionType() {
			return DominoSessionType.NATIVE;
		}
		@Override
		public void run() {
			Session session = getSession();
			Database database = session.getDatabase(databaseApiPath_);
			Document comment = database.getDocumentByUNID(commentId_);

			Document mailDoc = database.createDocument();
			mailDoc.replaceItemValue("Form", "Memo");
			// TODO move to config
			mailDoc.replaceItemValue("SendTo", "Jesse Gallagher/Frost");
			mailDoc.replaceItemValue("Subject", "New comment on " + database.getTitle());
			// TODO make adapt to server setup
			mailDoc.replaceItemValue("Body", "http://frostillic.us/f.nsf/posts/" + comment.getItemValueString("PostID"));
			mailDoc.send();


		}
	}

	@ManagedBean(name="Comments")
	@ApplicationScoped
	public static class Manager extends AbstractDominoManager<Comment> {
		private static final long serialVersionUID = 1L;

		public static Manager get() {
			Manager existing = (Manager)FrameworkUtils.resolveVariable(Manager.class.getAnnotation(ManagedBean.class).name());
			return existing == null ? new Manager() : existing;
		}

		@Override
		protected String getViewPrefix() {
			return "Comments\\";
		}
		@Override
		protected Database getDatabase() {
			AppConfig appConfig = AppConfig.get();
			String server = (String)appConfig.getValue("dataDatabaseServer");
			String filePath = (String)appConfig.getValue("dataDatabaseFilePath");
			return FrameworkUtils.getDatabase(server, filePath);
		}
	}
}
