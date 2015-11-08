package model;

import java.io.IOException;
import java.util.Date;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.openntf.domino.*;
import org.openntf.domino.xots.Xots;

import xots.CommentMessenger;

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

		setValue("Form", "Comment"); //$NON-NLS-1$ //$NON-NLS-2$
		Post post = (Post)FrameworkUtils.resolveVariable("post"); //$NON-NLS-1$
		if(post != null) {
			setValue("PostID", post.getValue("PostID")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	@Override
	protected boolean querySave() {
		if(isNew()) {
			setValue("Posted", new Date()); //$NON-NLS-1$
			setValue("CommentID", document().getUniversalID()); //$NON-NLS-1$

			HttpServletRequest req = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String remoteAddr = req.getRemoteAddr();
			String userAgent = req.getHeader("User-Agent"); //$NON-NLS-1$
			String referrer = req.getHeader("Referer"); //$NON-NLS-1$
			setValue("HTTP_Referer", referrer); //$NON-NLS-1$
			setValue("HTTP_User_Agent", userAgent); //$NON-NLS-1$
			setValue("Remote_Addr", remoteAddr); //$NON-NLS-1$

			Akismet akismet = Akismet.get();
			AppConfig appConfig = AppConfig.get();
			akismet.setApiKey((String)appConfig.getValue("akismetAPIKey")); //$NON-NLS-1$
			akismet.setBlog((String)appConfig.getValue("akismetBlog")); //$NON-NLS-1$

			DominoRichTextItem body = (DominoRichTextItem)getValue("body"); //$NON-NLS-1$
			try {
				boolean spam = akismet.checkComment(remoteAddr, userAgent, referrer, "", "comment", (String)getValue("authorName"), (String)getValue("authorEmailAddress"), (String)getValue("authorURL"), body.getHTML()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				setValue("AkismetSpam", spam ? 1 : 0); //$NON-NLS-1$
			} catch(IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		setValue("FullName", getValue("authorName")); //$NON-NLS-1$ //$NON-NLS-2$
		return super.querySave();
	}

	@Override
	protected void postSave() {
		Xots.getService().submit(new CommentMessenger(this));
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
			return "Comments\\"; //$NON-NLS-1$
		}
		@Override
		protected Database getDatabase() {
			AppConfig appConfig = AppConfig.get();
			String server = (String)appConfig.getValue("dataDatabaseServer"); //$NON-NLS-1$
			String filePath = (String)appConfig.getValue("dataDatabaseFilePath"); //$NON-NLS-1$
			return FrameworkUtils.getDatabase(server, filePath);
		}
	}
}
