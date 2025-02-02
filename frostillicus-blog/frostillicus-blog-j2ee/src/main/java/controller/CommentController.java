/*
 * Copyright (c) 2012-2025 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package controller;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import bean.AkismetBean;
import bean.MarkdownBean;
import bean.UserInfoBean;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.security.CsrfProtected;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import model.Comment;
import model.CommentRepository;
import model.PostRepository;

@Path("/posts/{postId}/comments")
@Controller
@RequestScoped
public class CommentController {
	private static final String GENERIC_USER = "frostillic.us Blog Commenter"; //$NON-NLS-1$
	private static final String GENERIC_EMAIL = "comment@frostillic.us"; //$NON-NLS-1$

	@Inject
	PostRepository posts;
	@Inject
	CommentRepository comments;

	@Inject
	MarkdownBean markdown;
	@Inject
	AkismetBean akismet;

	@Inject
	HttpServletRequest request;

	@POST
	@CsrfProtected
	public String create(
			@PathParam("postId") final String postId,
			@FormParam("postedBy") final String postedBy,
			@FormParam("bodyMarkdown") final String bodyMarkdown,
			@FormParam("postedByEmail") final String postedByEmail
			) throws Exception {
		posts.findPost(postId).orElseThrow(() -> new NotFoundException("Unable to find post matching ID " + postId)); //$NON-NLS-1$

		var remoteAddr = request.getRemoteAddr();
		var userAgent = request.getHeader("User-Agent"); //$NON-NLS-1$
		var referrer = request.getHeader("Referer"); //$NON-NLS-1$

		var comment = new Comment();
		comment.setCommentId(UUID.randomUUID().toString());
		comment.setPostId(postId);
		comment.setPosted(OffsetDateTime.now());
		comment.setPostedBy(postedBy);
		comment.setPostedByEmail(postedByEmail);
		comment.setBodyMarkdown(bodyMarkdown);
		comment.setHttpRemoteAddr(remoteAddr);
		comment.setHttpUserAgent(userAgent);
		comment.setHttpReferer(referrer);

		if(akismet.isValid()) {
			var spam = akismet.checkComment(remoteAddr, userAgent, referrer, "", AkismetBean.TYPE_COMMENT, GENERIC_USER, GENERIC_EMAIL, "", bodyMarkdown); //$NON-NLS-1$ //$NON-NLS-2$
			comment.setAkismetSpam(spam);
		}

		var html = markdown.toHtml(bodyMarkdown);
		html = Jsoup.clean(html, Safelist.basicWithImages());
		comment.setBodyHtml(html);

		comments.save(comment);

		return "redirect:posts/" + postId; //$NON-NLS-1$
	}

	@DELETE
	@Path("{commentId}")
	@RolesAllowed(UserInfoBean.ROLE_ADMIN)
	public String delete(@PathParam("postId") final String postId, @PathParam("commentId") final String commentId) {
		var comment = comments.findByCommentId(commentId).orElseThrow(() -> new NotFoundException("Unable to find comment matching ID " + commentId)); //$NON-NLS-1$
		comments.deleteById(comment.getId());
		return "redirect:posts/" + postId; //$NON-NLS-1$
	}
}
