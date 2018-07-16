package controller;

import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;
import javax.mvc.annotation.Controller;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.darwino.commons.json.JsonException;

import bean.MarkdownBean;
import model.Comment;
import model.CommentRepository;
import model.PostRepository;

@Path("/posts/{postId}/comments")
@Controller
public class CommentController {
	@Inject
	PostRepository posts;
	@Inject
	CommentRepository comments;
	
	@Inject
	MarkdownBean markdown;
	
	@POST
	public String create(@PathParam("postId") String postId, @FormParam("postedBy") String postedBy, @FormParam("bodyMarkdown") String bodyMarkdown) throws JsonException {
		posts.findByPostId(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId));
		
		Comment comment = new Comment();
		comment.setCommentId(UUID.randomUUID().toString());
		comment.setPostId(postId);
		comment.setPosted(new Date());
		comment.setPostedBy(postedBy);
		comment.setBodyMarkdown(bodyMarkdown);
		
		String html = markdown.toHtml(bodyMarkdown);
		html = Jsoup.clean(html, Whitelist.basicWithImages());
		comment.setBodyHtml(html);
		
		comments.save(comment);
		
		return "redirect:posts/" + postId;
	}
	
	@POST
	@Path("{commentId}")
	public String handlePost(@PathParam("postId") String postId, @PathParam("commentId") String commentId, @FormParam("_method") String methodOverride) {
		if("DELETE".equals(methodOverride)) {
			return delete(postId, commentId);
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	@DELETE
	@Path("{commentId}")
	public String delete(@PathParam("postId") String postId, @PathParam("commentId") String commentId) {
		Comment comment = comments.findByCommentId(commentId).orElseThrow(() -> new IllegalArgumentException("Unable to find comment matching ID " + commentId));
		comments.deleteById(comment.getId());
		return "redirect:posts/" + postId;
	}
}
