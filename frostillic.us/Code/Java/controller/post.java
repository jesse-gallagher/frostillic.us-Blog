package controller;

import java.util.Map;

import javax.faces.context.FacesContext;

import model.Comment;
import model.Post;

import org.openntf.domino.utils.DominoUtils;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.model.TabularDataModel;

import config.Translation;

import frostillicus.xsp.controller.BasicXPageController;
import frostillicus.xsp.model.AbstractModelList;
import frostillicus.xsp.util.FrameworkUtils;

public class post extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	public String getPostId() {
		String id = null;
		// First check the URL
		Map<String, String> param = FrameworkUtils.getParam();
		if(StringUtil.isNotEmpty(param.get("id"))) {
			id = param.get("id");
		}

		if(StringUtil.isEmpty(id)) {
			// Then check the path info
			String pathInfo = FacesContext.getCurrentInstance().getExternalContext().getRequestPathInfo();
			if(StringUtil.isNotEmpty(pathInfo)) {
				id = pathInfo.substring(1);
			}
		}

		if(StringUtil.isNotEmpty(id)) {
			if(!DominoUtils.isUnid(id)) {
				// Then it must be the URL slug - look it up in the view
				AbstractModelList<Post> posts = Post.Manager.get().getNamedCollection("All", null);
				posts.setResortOrder("$Key", TabularDataModel.SORT_ASCENDING);
				return posts.getByKey(id).getId();
			} else {
				return id;
			}
		}

		return "new";
	}

	public String postComment() {
		Comment comment = (Comment)FrameworkUtils.resolveVariable("comment");
		if(comment.save()) {
			FrameworkUtils.flashMessage("confirmation", Translation.get().getValue("commentPostConfirmation"));

			return "xsp-success";
		}
		return "xsp-failure";
	}
}