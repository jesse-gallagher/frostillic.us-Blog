package controller;

import frostillicus.JSFUtil;
import frostillicus.controller.BasicXPageController;
import java.util.*;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

public class Home extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() throws Exception {
		/*
		 * if param["p"]
			facesContext.external_context.redirect facesContext.externalContext.requestContextPath + "/posts/" + param[:p]
		elsif param[:category_name]
			facesContext.external_context.redirect facesContext.externalContext.requestContextPath + "/Tag.xsp?tag=" + Java::java.net.URLEncoder.encode(param[:category_name], "UTF-8")
		elsif param[:feed]
			facesContext.external_context.response.setStatus(301)
			#facesContext.external_context.redirect facesContext.externalContext.requestContextPath + "/feed.xml"
			facesContext.external_context.response.setHeader("Location", facesContext.externalContext.requestContextPath + "/feed.xml")
			facesContext.response_complete
		end
		 */

		FacesContext facesContext = FacesContext.getCurrentInstance();
		Map<String, String> param = JSFUtil.getParam();
		if(param.containsKey("p")) {
			facesContext.getExternalContext().redirect(facesContext.getExternalContext().getRequestContextPath() + "/posts/" + param.get("p"));
		} else if(param.containsKey("category_name")) {
			facesContext.getExternalContext().redirect(facesContext.getExternalContext().getRequestContextPath() + "/Tag.xsp?tag=" + java.net.URLEncoder.encode(param.get("p"), "UTF-8"));
		} else if(param.containsKey("feed")) {
			HttpServletResponse response = (HttpServletResponse)facesContext.getExternalContext().getResponse();
			response.setStatus(301);
			response.setHeader("Location", facesContext.getExternalContext().getRequestContextPath() + "/feed.xml");
			facesContext.responseComplete();
		}
	}
}
