package controller;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.mvc.Controller;
import javax.mvc.Models;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import bean.UserInfoBean;
import model.MicroPost;
import model.MicroPostRepository;

@Path("/microposts")
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
@Controller
@RequestScoped
public class MicroPostController {
	
	@Inject Models models;
	@Inject MicroPostRepository microPosts;
	
	@GET
	public String list() {
		models.put("posts", microPosts.findAll()); //$NON-NLS-1$
		
		return "microposts.jsp"; //$NON-NLS-1$
	}
	
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED })
	public String create(@FormParam("name") String name, @FormParam("content") String content) {
		MicroPost microPost = new MicroPost();
		microPost.setName(name);
		microPost.setContent(content);
		microPosts.save(microPost);
		
		return "redirect:microposts"; //$NON-NLS-1$
	}
}
