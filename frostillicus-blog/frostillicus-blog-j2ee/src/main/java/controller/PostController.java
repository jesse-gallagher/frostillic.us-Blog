/**
 * Copyright © 2016-2019 Jesse Gallagher
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

import bean.MarkdownBean;
import bean.UserInfoBean;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.StringUtil;
import model.CommentRepository;
import model.Post;
import model.util.PostUtil;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.mvc.Controller;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

@Path("/posts")
@Controller
@RequestScoped
public class PostController extends AbstractPostListController {
	@Inject
	HttpServletRequest request;

	@Inject
	CommentRepository comments;
	
	@Inject
	MarkdownBean markdown;

	@Inject
	UserInfoBean userInfo;
	
	@GET
	public String list(@QueryParam("start") String startParam) throws JsonException {
		String maybeList = maybeList(startParam);
		if(StringUtil.isNotEmpty(maybeList)) {
			return maybeList;
		} else {
			Map<Integer, Collection<Integer>> months = new TreeMap<>();
			for(String month : PostUtil.getPostMonths()) {
				int dash = month.indexOf('-');
				int y = Integer.parseInt(month.substring(0, dash));
				int m = Integer.parseInt(month.substring(dash+1), 10);

				months.computeIfAbsent(y, year -> new TreeSet<>()).add(m);
			}
			models.put("months", months); //$NON-NLS-1$
			return "posts-months.jsp"; //$NON-NLS-1$
		}
	}

	@GET
	@Path("{year}/{month}")
	public String listByMonth(@PathParam("year") int year, @PathParam("month") int month, @QueryParam("start") String startParam) {
		String monthQuery = String.format("%04d-%02d", year, month); //$NON-NLS-1$
		models.put("posts", posts.findByMonth(monthQuery)); //$NON-NLS-1$
		return "posts-list.jsp"; //$NON-NLS-1$
	}
	
	@GET
	@Path("tag/{tag}")
	public String byTag(@PathParam("tag") String tag) {
		models.put("tag", tag); //$NON-NLS-1$
		models.put("posts", posts.findByTag(tag)); //$NON-NLS-1$
		return "posts-bytag.jsp"; //$NON-NLS-1$
	}
	
	@GET
	@Path("new")
	@RolesAllowed(UserInfoBean.ROLE_ADMIN)
	public String compose() {
		models.put("post", new Post()); //$NON-NLS-1$

		return "post-new.jsp"; //$NON-NLS-1$
	}
	
	// TODO figure out if this can be done automatically without adding @FormParam to the model class
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED })
	@RolesAllowed(UserInfoBean.ROLE_ADMIN)
	public String create(@FormParam("title") String title, @FormParam("bodyMarkdown") String bodyMarkdown, @FormParam("tags") String tags, @FormParam("thread") String thread) {
		Post post = PostUtil.createPost();
		post.setPostedBy(userInfo.getDn());
		updatePost(post, bodyMarkdown, tags, title, thread);
		posts.save(post);
		
		return "redirect:posts/" + post.getPostId(); //$NON-NLS-1$
	}

	@GET
	@Path("{postId}")
	public String show(@PathParam("postId") String postId) {
		// IDs are often stored as lowercased UNIDs
		Post post = posts.findPost(postId)
				.orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		models.put("post", post); //$NON-NLS-1$
		
		models.put("comments", comments.findByPostId(post.getPostId())); //$NON-NLS-1$

		return "post.jsp"; //$NON-NLS-1$
	}
	
	@GET
	@Path("{year}/{month}/{day}/{postId}")
	public String showByDate(@PathParam("postId") String postId) {
		return show(postId);
	}
	
	@GET
	@Path("{year}/{month}/{day}/{postId}/edit")
	@RolesAllowed(UserInfoBean.ROLE_ADMIN)
	public String edit(@PathParam("postId") String postId) {
		Post post = posts.findPost(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		models.put("post", post); //$NON-NLS-1$
		return "post-edit.jsp"; //$NON-NLS-1$
	}

	@PUT
	@Path("{postId}")
	@RolesAllowed(UserInfoBean.ROLE_ADMIN)
	public String update(@PathParam("postId") String postId, @FormParam("title") String title, @FormParam("bodyMarkdown") String bodyMarkdown, @FormParam("tags") String tags, @FormParam("thread") String thread) {
		Post post = posts.findPost(postId)
				.orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		updatePost(post, bodyMarkdown, tags, title, thread);

		posts.save(post);

		return "redirect:posts/" + post.getPostId(); //$NON-NLS-1$
	}
	
	@DELETE
	@Path("{year}/{month}/{day}/{postId}")
	@RolesAllowed(UserInfoBean.ROLE_ADMIN)
	public String deleteByDate(@PathParam("postId") String postId) {
		return delete(postId);
	}
	
	@DELETE
	@Path("{postId}")
	@RolesAllowed(UserInfoBean.ROLE_ADMIN)
	public String delete(@PathParam("postId") String postId) {
		Post post = posts.findPost(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		posts.deleteById(post.getId());

		String referer = request.getHeader("Referer"); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(referer) && !referer.toLowerCase().contains(postId.toLowerCase())) {
			// TODO make this more robust?
			String context = request.getContextPath();
			int contextIndex = referer.indexOf(context);
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
	public String search(@QueryParam("q") String query) {
		models.put("posts", posts.search(query)); //$NON-NLS-1$
		return "search.jsp"; //$NON-NLS-1$
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************

	private void updatePost(Post post, String bodyMarkdown, String tags, String title, String thread) {
		if(StringUtil.isEmpty(post.getName())) {
			post.setName(StringUtil.toString(title).toLowerCase().replaceAll("\\s+", "-")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		post.setTitle(title);
		post.setBodyMarkdown(bodyMarkdown);
		post.setBodyHtml(markdown.toHtml(bodyMarkdown));
		post.setThread(thread);
		post.setTags(
				tags == null ? Collections.emptyList() :
						Arrays.stream(tags.split(",")) //$NON-NLS-1$
								.map(String::trim)
								.filter(StringUtil::isNotEmpty)
								.collect(Collectors.toList())
		);
	}
}
