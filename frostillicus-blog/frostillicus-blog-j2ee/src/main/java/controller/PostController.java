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
import javax.ws.rs.core.MediaType;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonObject;
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
			.query(JsonObject.of("form", Post.class.getSimpleName()))
			.findDocuments(doc -> {
				String posted = doc.getString("posted");
				if(posted != null && posted.length() >= 7) {
					months.add(posted.substring(0, 7));
				}
				return true;
			});
		
		models.put("months", months);
		
		return "posts.jsp";
	}
	
	@GET
	@Path("new")
	public String compose() {
		models.put("post", new Post());
		
		return "post-new.jsp";
	}
	
	// TODO figure out if this can be done automatically without adding @FormParam to the model class
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED })
	public String create(@FormParam("title") String title, @FormParam("bodyMarkdown") String bodyMarkdown, @FormParam("tags") String tags) throws JsonException {
		Post post = new Post();
		post.setPosted(new Date());
		post.setPostedBy(DarwinoContext.get().getSession().getUser().getDn());
		post.setTitle(title);
		post.setBodyMarkdown(bodyMarkdown);
		post.setBodyHtml(markdown.toHtml(bodyMarkdown));
		post.setTags(
			tags == null ? Collections.emptyList() :
			Arrays.stream(tags.split(",")).map(String::trim).collect(Collectors.toList())
		);
		
		post.setPostId(UUID.randomUUID().toString());
		posts.save(post);
		return "redirect:posts/" + post.getPostId();
	}
	
	@GET
	@Path("{postId}")
	public String show(@PathParam("postId") String postId) {
		Post post = posts.findByPostId(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId));
		models.put("post", post);
		
		models.put("comments", comments.findByPostId(post.getPostId()));
		
		return "post.jsp";
	}
	
	@GET
	@Path("{year}/{month}/{day}/{postId}")
	public String showByDate(@PathParam("postId") String postId) {
		return show(postId);
	}
	
	@GET
	@Path("{year}/{month}/{day}/{postId}/edit")
	public String edit(@PathParam("postId") String postId) {
		Post post = posts.findByPostId(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId));
		models.put("post", post);
		return "post-edit.jsp";
	}
	
	@POST
	@Path("{postId}")
	public String handlePost(@PathParam("postId") String postId, @FormParam("_method") String methodOverride) {
		if("DELETE".equals(methodOverride)) {
			return delete(postId);
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	@DELETE
	@Path("{postId}")
	public String delete(@PathParam("postId") String postId) {
		Post post = posts.findByPostId(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId));
		posts.deleteById(post.getId());
		return "redirect:posts";
	}
}
