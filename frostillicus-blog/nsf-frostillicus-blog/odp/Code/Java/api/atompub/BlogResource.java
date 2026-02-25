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
package api.atompub;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;

import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewQuery;

import api.atompub.model.AtomCategory;
import api.atompub.model.Author;
import api.atompub.model.Content;
import api.atompub.model.Control;
import api.atompub.model.Entry;
import api.atompub.model.Feed;
import api.atompub.model.Link;
import api.atompub.model.Summary;
import bean.UserInfoBean;
import controller.PostController;
import jakarta.annotation.security.RolesAllowed;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import model.Post;
import model.util.PostUtil;

@Path(AtomPubResource.BASE_PATH + "/{blogId}")
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
public class BlogResource {
	public static final int PAGE_LENGTH = 100;

	@Inject
	@Named("translation")
	ResourceBundle translation;

	@Context
	UriInfo uriInfo;

	@Inject
	Post.PostRepository posts;

	@Inject
	UserInfoBean userInfo;

	@GET
	@Produces("application/atom+xml")
	public Feed get(@QueryParam("start") final String startParam) {
		var feed = new Feed();
		feed.setTitle(translation.getString("appTitle")); //$NON-NLS-1$
		feed.setSubtitle(translation.getString("appDescription")); //$NON-NLS-1$
		feed.setId(resolveUrl(AtomPubResource.BLOG_ID));
		feed.getLinks().add(new Link("alternate", translation.getString("baseUrl"))); //$NON-NLS-1$ //$NON-NLS-2$
		feed.getLinks().add(new Link("first", resolveUrl(AtomPubResource.BLOG_ID))); //$NON-NLS-1$

		// Figure out the starting point
		var start = Math.max(PostUtil.parseStartParam(startParam), 0);
		var result = posts.homeList(PageRequest.ofPage((start / PAGE_LENGTH) + 1, PAGE_LENGTH, false));

		if (start + PAGE_LENGTH < PostUtil.getPostCount()) {
			// Then add nav links
			var next = new Link();
			next.setRel("next"); //$NON-NLS-1$
			next.setHref(resolveUrl(AtomPubResource.BLOG_ID) + "?start=" + (start + PAGE_LENGTH)); //$NON-NLS-1$
			feed.getLinks().add(next);
		}

		result.stream()
			.map(this::toEntry)
			.forEach(feed.getEntries()::add);

		return feed;
	}

	@POST
	@Produces("application/atom+xml")
	public Response post(final Entry entry) throws URISyntaxException {

		var post = PostUtil.createPost();
		post.setPostedBy(userInfo.getDn());
		updatePost(post, entry);

		var result = toEntry(post);
		return Response.created(new URI(resolveUrl(AtomPubResource.BLOG_ID, post.getPostId())))
			.entity(result)
			.build();
	}

	@GET
	@Path("{entryId}")
	@Produces("application/atom+xml")
	public Entry getEntry(@PathParam("entryId") final String postId) {
		var post = posts.findByPostId(ViewQuery.query().key(postId, true))
				.orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		return toEntry(post);
	}

	@PUT
	@Path("{entryId}")
	public Response updateEntry(@PathParam("entryId") final String postId, final Entry entry) {
		var post = posts.findByPostId(ViewQuery.query().key(postId, true))
				.orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		updatePost(post, entry);
		return Response.ok().build();
	}

	@DELETE
	@Path("{entryId}")
	public Response deleteEntry(@PathParam("entryId") final String postId) {
		var post = posts.findByPostId(ViewQuery.query().key(postId, true))
				.orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		posts.deleteById(post.getId());
		return Response.ok().build();
	}

	private Entry toEntry(final Post post) {
		var entry = new Entry();
		entry.setAuthor(new Author(post.getPostedBy()));
		entry.setTitle(post.getTitle());
		entry.setPublished(post.getPosted().toInstant());
		var mod = post.getModified().toInstant();
		entry.setUpdated(mod == null ? entry.getPublished() : mod);
		entry.setTitle(StringUtil.toString(post.getTitle()));
		if (post.getStatus() == Post.Status.Draft) {
			entry.setControl(new Control("yes")); //$NON-NLS-1$
		}

		var bodyMarkdown = post.getBodyMarkdown();

		if (StringUtil.isNotEmpty(bodyMarkdown)) {
			var markdown = new Content();
			markdown.setType("text/markdown"); //$NON-NLS-1$
			markdown.setValue(bodyMarkdown);
			entry.setContent(markdown);
		} else {
			var content = new Content();
			content.setType(MediaType.TEXT_HTML);
			content.setValue(post.getBodyHtml());
			entry.setContent(content);
		}

		var summary = post.getSummary();
		if(StringUtil.isNotEmpty(summary)) {
			var summaryElement = new Summary();
			summaryElement.setType(MediaType.TEXT_PLAIN);
			summaryElement.setBody(summary);
			entry.setSummary(summaryElement);
		}

		var tags = post.getTags();
		if(tags != null) {
			tags.stream()
				.map(AtomCategory::new)
				.forEach(entry.getCategories()::add);
		}

		// Add links
		var read = new Link();
		var postsRoot = PostController.class.getAnnotation(Path.class).value();
		read.setHref(resolveUrlRoot(postsRoot, post.getPostId()));
		entry.getLinks().add(read);
		var edit = new Link();
		edit.setHref(resolveUrl(AtomPubResource.BLOG_ID, post.getPostId()));
		edit.setRel("edit"); //$NON-NLS-1$
		entry.getLinks().add(edit);

		return entry;
	}

	private void updatePost(final Post post, final Entry entry) {
		var posted = true;
		if(entry.getControl() != null) {
			posted = !"yes".equals(entry.getControl().getDraft()); //$NON-NLS-1$
		}
		post.setTitle(entry.getTitle());
		post.setBodyMarkdown(entry.getContent().getValue());
		var summary = entry.getSummary();
		if(summary != null) {
			post.setSummary(summary.getBody());
		}
		var categories = entry.getCategories();
		if(categories != null) {
			post.setTags(categories.stream().map(AtomCategory::getTerm).collect(Collectors.toList()));
		}
		post.setStatus(posted ? Post.Status.Posted : Post.Status.Draft);
		post.setModified(OffsetDateTime.now());
		posts.save(post);
	}

	private String resolveUrl(final String... parts) {
		var baseUri = uriInfo.getBaseUri();
		var uri = PathUtil.concat(baseUri.toString(), AtomPubResource.BASE_PATH, '/');
		for (String part : parts) {
			uri = PathUtil.concat(uri, part, '/');
		}
		return uri;
	}

	private String resolveUrlRoot(final String... parts) {
		var baseUri = uriInfo.getBaseUri();
		var uri = baseUri.toString();
		for (String part : parts) {
			uri = PathUtil.concat(uri, part, '/');
		}
		return uri;
	}
}
