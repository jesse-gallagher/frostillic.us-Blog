package controller;

import javax.inject.Inject;
import javax.mvc.Models;
import javax.mvc.annotation.Controller;
import javax.ws.rs.BeanParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.darwino.commons.json.JsonException;
import com.darwino.platform.DarwinoContext;

import model.Post;
import model.PostRepository;

@Path("/posts")
@Controller
public class PostController {
	@Inject
	Models models;
	
	@Inject
	PostRepository posts;
	
	@GET
	public String list() {
		return "posts.jsp";
	}
	
	@GET
	@Path("new")
	public String compose() {
		return "post-new.jsp";
	}
	
	// TODO figure out if this can be done automatically without adding @FormParam to the model class
	@POST
	public String create(@FormParam("title") String title, @FormParam("bodyMarkdown") String bodyMarkdown) throws JsonException {
		Post post = new Post();
		post.setPostedBy(DarwinoContext.get().getSession().getUser().getDn());
		post.setTitle(title);
		post.setBodyMarkdown(bodyMarkdown);
		posts.save(post);
		return "posted.jsp";
	}
	
	@GET
	@Path("{postId}")
	public String show(@PathParam("postId") String postId) {
		Post post = posts.findById(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId));
		models.put("post", post);
		return "post.jsp";
	}
}
