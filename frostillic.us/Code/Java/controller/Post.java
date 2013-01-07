package controller;

import javax.faces.context.FacesContext;
import lotus.domino.*;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.http.MimeMultipart;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;

import frostillicus.JSFUtil;
import frostillicus.Akismet;
import frostillicus.event.SimpleEventDispatcher;
import frostillicus.controller.BasicDocumentController;
import java.util.*;
import lombok.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class Post extends BasicDocumentController {
	private static final long serialVersionUID = 1L;

	@Getter private Map<String, Object> newCommentData = new HashMap<String, Object>();

	@Override
	public void beforePageLoad() throws UnsupportedEncodingException {
		DominoDocument doc = this.getDoc();
		UIViewRootEx2 view = (UIViewRootEx2)FacesContext.getCurrentInstance().getViewRoot();
		view.setPageTitle((String)doc.getValue("$$Title"));


		// Fill in default values for the new comment data
		Map<String, Cookie> cookie = JSFUtil.getCookie();
		if(cookie.containsKey("AuthorName")) { newCommentData.put("AuthorName", URLDecoder.decode(cookie.get("AuthorName").getValue(), "UTF-8")); }
		if(cookie.containsKey("AuthorEmail")) { newCommentData.put("AuthorEmail", URLDecoder.decode(cookie.get("AuthorName").getValue(), "UTF-8")); }
		if(cookie.containsKey("AuthorURL")) { newCommentData.put("AuthorURL", URLDecoder.decode(cookie.get("AuthorName").getValue(), "UTF-8")); }
	}

	public String deletePost() throws NotesException {
		Document doc = this.getDoc().getDocument();
		doc.replaceItemValue("Form", "Deleted Post");
		doc.save();
		return "xsp-success";
	}

	public String submitComment() throws Exception {
		FacesContext facesContext = FacesContext.getCurrentInstance();

		HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
		String remoteAddr = request.getRemoteAddr();
		String userAgent = request.getHeader("User-Agent");
		String referrer = request.getHeader("Referer");

		// Set up the Akismet requester and see if it's spam
		Database database = ExtLibUtil.getCurrentDatabase();
		View config = database.getView("Configuration");
		Document akismetConfig = config.getDocumentByKey("Akismet", true);
		Akismet akismet = (Akismet)resolveVariable("akismet");
		akismet.setApiKey(akismetConfig.getItemValueString("AkismetAPIKey"));
		akismet.setBlog(akismetConfig.getItemValueString("AkismetBlog"));

		MimeMultipart body = (MimeMultipart)newCommentData.get("body");
		boolean spam = akismet.checkComment(remoteAddr, userAgent, referrer, "", "comment", (String)newCommentData.get("name"), (String)newCommentData.get("email"), (String)newCommentData.get("url"), body.getHTML());

		// Create the comment doc
		Document comment = database.createDocument();
		comment.replaceItemValue("Form", "Comment");
		comment.replaceItemValue("PostID", this.getDoc().getValue("PostID"));
		comment.replaceItemValue("AkismetSpam", spam ? 1 : 0);

		comment.replaceItemValue("Remote_Addr", remoteAddr);
		comment.replaceItemValue("HTTP_User_Agent", userAgent);
		comment.replaceItemValue("HTTP_Referer", referrer);
		comment.replaceItemValue("AuthorName", newCommentData.get("name"));
		comment.replaceItemValue("AuthorEmailAddress", newCommentData.get("email"));
		comment.replaceItemValue("Body", body.getHTML());

		comment.computeWithForm(true, false);
		comment.save();

		if(!spam) {
			SimpleEventDispatcher applicationDispatcher = (SimpleEventDispatcher)resolveVariable("applicationDispatcher");
			applicationDispatcher.dispatch("comment posted", comment.getUniversalID());
		}

		newCommentData.clear();

		facesContext.getExternalContext().redirect(facesContext.getExternalContext().getRequestContextPath() + "/posts/" + this.getDoc().getValue("PostID"));

		return "xsp-success";
	}
}
