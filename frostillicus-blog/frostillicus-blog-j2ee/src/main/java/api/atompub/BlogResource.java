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
package api.atompub;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jakarta.annotation.security.RolesAllowed;
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
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.PathUtil;
import com.darwino.commons.util.StringUtil;
import com.darwino.commons.xml.DomUtil;
import com.darwino.commons.xml.XPathUtil;
import com.darwino.jsonstore.Session;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndCategoryImpl;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.feed.synd.SyndLinkImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

import bean.UserInfoBean;
import controller.PostController;
import model.Post;
import model.Post.Status;
import model.PostRepository;
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
	PostRepository posts;

	@Inject
	Session darwinoSession;

	@GET
	@Produces("application/atom+xml")
	public String get(@QueryParam("start") final String startParam) throws FeedException, JsonException {
		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("atom_1.0"); //$NON-NLS-1$
		feed.setTitle(translation.getString("appTitle")); //$NON-NLS-1$
		feed.setDescription(translation.getString("appDescription")); //$NON-NLS-1$
		feed.setLink(translation.getString("baseUrl")); //$NON-NLS-1$
		feed.setUri(resolveUrl(AtomPubResource.BLOG_ID));

		// Figure out the starting point
		var start = Math.max(PostUtil.parseStartParam(startParam), 0);
		var result = posts.homeList(start, PAGE_LENGTH);

		// Add a nav link
		List<SyndLink> links = new ArrayList<>();
		SyndLink first = new SyndLinkImpl();
		first.setRel("first"); //$NON-NLS-1$
		first.setHref(resolveUrl(AtomPubResource.BLOG_ID));
		links.add(first);

		if (start + PAGE_LENGTH < PostUtil.getPostCount()) {
			// Then add nav links
			SyndLink next = new SyndLinkImpl();
			next.setRel("next"); //$NON-NLS-1$
			next.setHref(resolveUrl(AtomPubResource.BLOG_ID) + "?start=" + (start + PAGE_LENGTH)); //$NON-NLS-1$
			links.add(next);
		}
		feed.setLinks(links);

		var output = new SyndFeedOutput().outputW3CDom(feed);
		var target = output.getDocumentElement();

		result.stream().map(post -> {
			try {
				return toAtomXml(post);
			} catch (XPathExpressionException | FeedException e) {
				throw new RuntimeException(e);
			}
		}).map(e -> output.importNode(e, true)).forEach(target::appendChild);

		return DomUtil.getXMLString(output);
	}

	@POST
	@Produces("application/atom+xml")
	public Response post(final Document xml)
			throws XPathExpressionException, JsonException, URISyntaxException, FeedException {

		var post = PostUtil.createPost();
		post.setPostedBy(darwinoSession.getUser().getDn());
		updatePost(post, xml);

		return Response.created(new URI(resolveUrl(AtomPubResource.BLOG_ID, post.getPostId()))).entity(toAtomXml(post))
				.build();
	}

	@GET
	@Path("{entryId}")
	@Produces("application/atom+xml")
	public String getEntry(@PathParam("entryId") final String postId) throws FeedException, XPathExpressionException {
		var post = posts.findPost(postId)
				.orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		return DomUtil.getXMLString(toAtomXml(post), false, true);
	}

	@PUT
	@Path("{entryId}")
	public Response updateEntry(@PathParam("entryId") final String postId, final Document xml) throws XPathExpressionException {
		var post = posts.findPost(postId)
				.orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		updatePost(post, xml);
		return Response.ok().build();
	}

	@DELETE
	@Path("{entryId}")
	public Response deleteEntry(@PathParam("entryId") final String postId) {
		// TODO figure out why this doesn't work with existing posts. I imagine it's to
		// do with Darwino's treatment of editors
		var post = posts.findPost(postId)
				.orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
		posts.deleteById(post.getId());
		return Response.ok().build();
	}

	private SyndEntry toEntry(final Post post) {
		SyndEntry entry = new SyndEntryImpl();
		entry.setAuthor(post.getPostedBy());
		entry.setTitle(post.getTitle());
		entry.setPublishedDate(Date.from(post.getPosted().toInstant()));
		var mod = post.getModified();
		entry.setUpdatedDate(mod == null ? entry.getPublishedDate() : Date.from(mod.toInstant()));

		SyndContent description = new SyndContentImpl();
		description.setType(MediaType.TEXT_PLAIN);
		description.setValue(StringUtil.toString(post.getSummary()));
		entry.setDescription(description);

		List<SyndContent> contents = new ArrayList<>();

		var bodyMarkdown = post.getBodyMarkdown();

		if (StringUtil.isNotEmpty(bodyMarkdown)) {
			SyndContent markdown = new SyndContentImpl();
			markdown.setType("text/markdown"); //$NON-NLS-1$
			markdown.setValue(bodyMarkdown);
			contents.add(markdown);
		} else {
			SyndContent content = new SyndContentImpl();
			content.setType(MediaType.TEXT_HTML);
			content.setValue(post.getBodyHtml());
			contents.add(content);
		}

		entry.setContents(contents);

		entry.setCategories(post.getTags().stream().map(this::toCategory).collect(Collectors.toList()));

		// Add links
		SyndLink read = new SyndLinkImpl();
		var postsRoot = PostController.class.getAnnotation(Path.class).value();
		read.setHref(resolveUrlRoot(postsRoot, post.getPostId()));
		SyndLink edit = new SyndLinkImpl();
		edit.setHref(resolveUrl(AtomPubResource.BLOG_ID, post.getPostId()));
		edit.setRel("edit"); //$NON-NLS-1$
		entry.setLinks(Arrays.asList(read, edit));

		return entry;
	}

	private Element toAtomXml(final Post post) throws FeedException, XPathExpressionException {
		var entry = toEntry(post);
		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("atom_1.0"); //$NON-NLS-1$
		feed.setEntries(Arrays.asList(entry));
		var feedDoc = new SyndFeedOutput().outputW3CDom(feed);
		var entryElement = (Element) XPathUtil.node(feedDoc, "/*[name()='feed']/*[name()='entry']"); //$NON-NLS-1$

		if (post.getStatus() == Status.Draft) {
			var control = DomUtil.createElement(entryElement, "app:control"); //$NON-NLS-1$
			control.setAttribute("xmlns:app", "http://www.w3.org/2007/app"); //$NON-NLS-1$ //$NON-NLS-2$
			DomUtil.createElement(control, "app:draft", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return entryElement;
	}

	private SyndCategory toCategory(final String tag) {
		SyndCategory cat = new SyndCategoryImpl();
		cat.setName(tag);
		return cat;
	}

	private void updatePost(final Post post, final Document xml) throws XPathExpressionException {
		// TODO convert to ROME

		var title = XPathUtil.node(xml, "/*[name()='entry']/*[name()='title']").getTextContent(); //$NON-NLS-1$
		var body = XPathUtil.node(xml, "/*[name()='entry']/*[name()='content']").getTextContent(); //$NON-NLS-1$
		var summary = XPathUtil.node(xml, "/*[name()='entry']/*[name()='summary']").getTextContent(); //$NON-NLS-1$
		var tagsNodes = XPathUtil.nodes(xml, "/*[name()='entry']/*[name()='category']"); //$NON-NLS-1$
		List<String> tags = IntStream.range(0, tagsNodes.getLength()).mapToObj(tagsNodes::item).map(Element.class::cast)
				.map(el -> el.getAttribute("term")) //$NON-NLS-1$
				.collect(Collectors.toList());

		var posted = !"yes".equals(XPathUtil //$NON-NLS-1$
				.node(xml, "*[name()='entry']/*[name()='app:control']/*[name()='app:draft']").getTextContent()); //$NON-NLS-1$
		post.setTitle(title);
		post.setBodyMarkdown(body);
		post.setSummary(summary);
		post.setTags(tags);
		post.setStatus(posted ? Post.Status.Posted : Post.Status.Draft);
		posts.save(post);
	}

	private String resolveUrl(final String... parts) {
		var baseUri = uriInfo.getBaseUri();
		var uri = PathUtil.concat(baseUri.toString(), AtomPubResource.BASE_PATH);
		for (String part : parts) {
			uri = PathUtil.concat(uri, part);
		}
		return uri;
	}

	private String resolveUrlRoot(final String... parts) {
		var baseUri = uriInfo.getBaseUri();
		var uri = baseUri.toString();
		for (String part : parts) {
			uri = PathUtil.concat(uri, part);
		}
		return uri;
	}
}
