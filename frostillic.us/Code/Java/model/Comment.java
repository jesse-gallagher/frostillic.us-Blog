package model;

import java.util.Date;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.openntf.domino.Database;

import config.AppConfig;

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
			setValue("HTTP_Referer", req.getHeader("Referer"));
			setValue("HTTP_User_Agent", req.getHeader("User-Agent"));
			setValue("Remote_Addr", req.getRemoteAddr());
		}
		return super.querySave();
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
