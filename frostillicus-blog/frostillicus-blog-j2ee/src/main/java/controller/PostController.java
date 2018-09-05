/**
 * Copyright © 2016-2018 Jesse Gallagher
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
import java.util.Date;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.mvc.Models;
import javax.mvc.annotation.Controller;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.util.StringUtil;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Store;
import com.darwino.platform.DarwinoContext;

import bean.MarkdownBean;
import frostillicus.blog.app.AppDatabaseDef;
import model.CommentRepository;
import model.Post;
import model.PostRepository;

@Path("/posts")
@Controller
public class PostController {
	@Inject
	Models models;
	
	@Inject
	PostRepository posts;
	@Inject
	CommentRepository comments;
	
	@Inject
	MarkdownBean markdown;
	
	@Inject
	Database database;
	
	@GET
	public String list() throws JsonException {
		Collection<String> months = new TreeSet<String>();
		
		// Fetch the months - use Darwino directly for this for now
		Store store = database.getStore(AppDatabaseDef.STORE_POSTS);
		store.openCursor()
			.query(JsonObject.of("form", Post.class.getSimpleName())) //$NON-NLS-1$
			.findDocuments(doc -> {
				String posted = doc.getString("posted"); //$NON-NLS-1$
				if(posted != null && posted.length() >= 7) {
					months.add(posted.substring(0, 7));
				}
				return true;
			});
		
		models.put("months", months); //$NON-NLS-1$
		
		return "posts.jsp"; //$NON-NLS-1$
	}
	
	@GET
	@Path("tag/{tag}")
	public String byTag(@PathParam("tag") String tag) {
		models.put("tag", tag); //$NON-NLS-1$
		models.put("posts", posts.findByTag(tag)); //$NON-NLS-1$
		// TODO figure out how to do this in a query
//		models.put("posts",
//			posts.findAll().stream()
//				.filter(p -> p.getTags() != null && p.getTags().contains(tag))
//				.collect(Collectors.toList())
//		);
		
		// TODO make standalone page
		return "home.jsp"; //$NON-NLS-1$
	}
	
	@GET
	@Path("new")
	public String compose() {
		models.put("post", new Post()); //$NON-NLS-1$
		
		return "post-new.jsp"; //$NON-NLS-1$
	}
	
	// TODO figure out if this can be done automatically without adding @FormParam to the model class
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED })
	@RolesAllowed("admin")
	public String create(@FormParam("title") String title, @FormParam("bodyMarkdown") String bodyMarkdown, @FormParam("tags") String tags) throws JsonException {
		Post post = new Post();
		post.setPosted(new Date());
		post.setPostedBy(DarwinoContext.get().getSession().getUser().getDn());
		post.setTitle(title);
		post.setBodyMarkdown(bodyMarkdown);
		post.setBodyHtml(markdown.toHtml(bodyMarkdown));
		post.setTags(
			tags == null ? Collections.emptyList() :
			Arrays.stream(tags.split(",")) //$NON-NLS-1$
				.map(String::trim)
				.filter(StringUtil::isNotEmpty)
				.collect(Collectors.toList())
		);
		
		post.setPostId(UUID.randomUUID().toString());
		posts.save(post);
		
		return "redirect:posts/" + post.getPostId(); //$NON-NLS-1$
	}
	
	@GET
	@Path("{postId}")
	public String show(@PathParam("postId") String postId) {
		Post post = posts.findByPostId(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
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
	@RolesAllowed("admin")
	public String edit(@PathParam("postId") String postId) {
		Post post = posts.findByPostId(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		models.put("post", post); //$NON-NLS-1$
		return "post-edit.jsp"; //$NON-NLS-1$
	}
	
	@DELETE
	@Path("{year}/{month}/{day}/{postId}")
	@RolesAllowed("admin")
	public String deleteByDate(@PathParam("postId") String postId) {
		return delete(postId);
	}
	
	@DELETE
	@Path("{postId}")
	@RolesAllowed("admin")
	public String delete(@PathParam("postId") String postId) {
		Post post = posts.findByPostId(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		posts.deleteById(post.getId());
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
}
