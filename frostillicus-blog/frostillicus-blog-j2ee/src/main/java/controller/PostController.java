/*
 * Copyright © 2012-2022 Jesse Gallagher
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.StringUtil;

import bean.UserInfoBean;
import model.Comment;
import model.CommentRepository;
import model.Post;
import model.Post.Status;
import model.util.PostUtil;

@Path(PostController.PATH)
@Controller
@RequestScoped
public class PostController extends AbstractPostListController {
	public static final String PATH = "/posts"; //$NON-NLS-1$

	@Inject
	HttpServletRequest request;

	@Inject
	CommentRepository comments;

	@Inject
	UserInfoBean userInfo;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String list(@QueryParam("start") final String startParam) throws JsonException {
		var maybeList = maybeList(startParam);
		if(StringUtil.isNotEmpty(maybeList)) {
			return maybeList;
		} else {
			Map<Integer, Collection<Integer>> months = new TreeMap<>(Comparator.reverseOrder());
			for(String month : PostUtil.getPostMonths()) {
				var dash = month.indexOf('-');
				var y = Integer.parseInt(month.substring(0, dash));
				var m = Integer.parseInt(month.substring(dash+1), 10);

				months.computeIfAbsent(y, year -> new TreeSet<>(Comparator.reverseOrder())).add(m);
			}
			models.put("months", months); //$NON-NLS-1$
			return "posts-months.jsp"; //$NON-NLS-1$
		}
	}

	@GET
	@Path("{year}/{month}")
	@Produces(MediaType.TEXT_HTML)
	public String listByMonth(@PathParam("year") final int year, @PathParam("month") final int month, @QueryParam("start") final String startParam) {
		var monthQuery = String.format("%04d-%02d", year, month); //$NON-NLS-1$
		models.put("posts", posts.findByMonth(monthQuery)); //$NON-NLS-1$
		return "posts-list.jsp"; //$NON-NLS-1$
	}

	@GET
	@Path("tag/{tag}")
	@Produces(MediaType.TEXT_HTML)
	public String byTag(@PathParam("tag") final String tag) {
		models.put("tag", tag); //$NON-NLS-1$
		models.put("posts", posts.findByTag(tag)); //$NON-NLS-1$
		return "posts-bytag.jsp"; //$NON-NLS-1$
	}

	@GET
	@Path("new")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed(UserInfoBean.ROLE_ADMIN)
	public String compose() {
		models.put("post", new Post()); //$NON-NLS-1$

		return "post-new.jsp"; //$NON-NLS-1$
	}

	// TODO figure out if this can be done automatically without adding @FormParam to the model class
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED })
	@RolesAllowed(UserInfoBean.ROLE_ADMIN)
	public String create(
			@FormParam("title") final String title,
			@FormParam("bodyMarkdown") final String bodyMarkdown,
			@FormParam("tags") final String tags,
			@FormParam("thread") final String thread,
			@FormParam("status") final String status) {
		var post = PostUtil.createPost();
		post.setPostedBy(userInfo.getDn());
		updatePost(post, bodyMarkdown, tags, title, thread, status);
		posts.save(post);

		return "redirect:posts/" + post.getSlug(); //$NON-NLS-1$
	}

	@GET
	@Path("{postId}")
	@Produces(MediaType.TEXT_HTML)
	public String show(@PathParam("postId") final String postId) {
		var post = posts.findPost(postId)
				.orElseThrow(() -> new NotFoundException("Unable to find post matching ID " + postId)); //$NON-NLS-1$

		return showPost(post);
	}

	@GET
	@Path("{year}/{month}/{day}/{postId}")
	@Produces(MediaType.TEXT_HTML)
	public String showByDate(@PathParam("year") final int year, @PathParam("month") final int month, @PathParam("day") final int day, @PathParam("postId") final String postId) {
		var post = posts.findPost(postId)
				.orElseThrow(() -> new NotFoundException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		if(!post.matchesPostedDate(year, month, day)) {
			throw new NotFoundException();
		}
		return show(postId);
	}

	@GET
	@Path("{year}/{month}/{day}/{postId}/edit")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed(UserInfoBean.ROLE_ADMIN)
	public String edit(@PathParam("postId") final String postId) {
		var post = posts.findPost(postId).orElseThrow(() -> new NotFoundException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		models.put("post", post); //$NON-NLS-1$
		return "post-edit.jsp"; //$NON-NLS-1$
	}

	@PUT
	@Path("{postId}")
	@RolesAllowed(UserInfoBean.ROLE_ADMIN)
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED })
	public String update(
			@PathParam("postId") final String postId,
			@FormParam("title") final String title,
			@FormParam("bodyMarkdown") final String bodyMarkdown,
			@FormParam("tags") final String tags,
			@FormParam("thread") final String thread,
			@FormParam("status") final String status) {
		var post = posts.findPost(postId)
				.orElseThrow(() -> new NotFoundException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		updatePost(post, bodyMarkdown, tags, title, thread, status);

		posts.save(post);

		return "redirect:posts/" + post.getSlug(); //$NON-NLS-1$
	}

	@DELETE
	@Path("{year}/{month}/{day}/{postId}")
	@RolesAllowed(UserInfoBean.ROLE_ADMIN)
	public String deleteByDate(@PathParam("postId") final String postId) {
		return delete(postId);
	}

	@DELETE
	@Path("{postId}")
	@RolesAllowed(UserInfoBean.ROLE_ADMIN)
	public String delete(@PathParam("postId") final String postId) {
		var post = posts.findPost(postId).orElseThrow(() -> new NotFoundException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		var id = post.getPostId();
		posts.deleteById(post.getId());
		comments.deleteById(comments.findByPostId(id).stream()
			.map(Comment::getId)
			.collect(Collectors.toList()));

		var referer = request.getHeader("Referer"); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(referer) && !referer.toLowerCase().contains(postId.toLowerCase())) {
			// TODO make this more robust?
			var context = request.getContextPath();
			var contextIndex = referer.indexOf(context);
			if(contextIndex > -1) {
				return "redirect:" + referer.substring(contextIndex + context.length()); //$NON-NLS-1$
			}
		}
		return "redirect:posts"; //$NON-NLS-1$
	}

	// *******************************************************************************
	// * Searching
	// *******************************************************************************
	@GET
	@Path("search")
	@Produces(MediaType.TEXT_HTML)
	public String search(@QueryParam("q") final String query) {
		models.put("posts", posts.search(query)); //$NON-NLS-1$
		return "search.jsp"; //$NON-NLS-1$
	}

	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************

	private void updatePost(final Post post, final String bodyMarkdown, final String tags, final String title, final String thread, final String statusParam) {
		var status = Status.valueFor(statusParam);
		post.setTitle(title);
		post.setBodyMarkdown(bodyMarkdown);
		post.setThread(thread);
		post.setStatus(status);
		post.setTags(
			tags == null ? Collections.emptyList() :
				Arrays.stream(tags.split(",")) //$NON-NLS-1$
					.map(String::trim)
					.filter(StringUtil::isNotEmpty)
					.collect(Collectors.toList())
		);
	}

	private String showPost(final Post post) {
		models.put("post", post); //$NON-NLS-1$
		models.put("comments", comments.findByPostId(post.getPostId())); //$NON-NLS-1$

		return "post.jsp"; //$NON-NLS-1$
	}
}
