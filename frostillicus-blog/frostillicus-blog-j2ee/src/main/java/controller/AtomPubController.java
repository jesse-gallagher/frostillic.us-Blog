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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.darwino.commons.util.PathUtil;
import com.darwino.commons.util.StringUtil;
import com.darwino.commons.xml.DomUtil;
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

import frostillicus.blog.app.AppDatabaseDef;
import lombok.SneakyThrows;
import model.Post;
import model.PostRepository;

@Path(AtomPubController.BASE_PATH)
@RolesAllowed("admin")
public class AtomPubController {
	static final String BASE_PATH = "/atompub"; //$NON-NLS-1$
	
	static final String BLOG_ID = AppDatabaseDef.DATABASE_NAME;
	
	@Inject
	PostRepository posts;
	@Inject @Named("translation")
	ResourceBundle translation;
	@Context
	ServletContext servletContext;
	@Context
	HttpServletRequest servletRequest;
	
	// This only supports the one active blog
	@GET
	@Produces("application/atomserv+xml")
	public String getWorkspace() {
		Document xml = DomUtil.createDocument();
		Element service = DomUtil.createRootElement(xml, "service"); //$NON-NLS-1$
		service.setAttribute("xmlns", "http://purl.org/atom/app#"); //$NON-NLS-1$ //$NON-NLS-2$
		service.setAttribute("xmlns:atom", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$
		Element workspace = DomUtil.createElement(service, "workspace"); //$NON-NLS-1$
		DomUtil.createElement(workspace, "atom:title", BLOG_ID); //$NON-NLS-1$
		Element collection = DomUtil.createElement(workspace, "collection"); //$NON-NLS-1$
		collection.setAttribute("href", resolveUrl(BLOG_ID)); //$NON-NLS-1$
		DomUtil.createElement(collection, "atom:title", "Entries"); //$NON-NLS-1$ //$NON-NLS-2$
		return DomUtil.getXMLString(xml);
	}
	
	@GET
	@Path(BLOG_ID)
	@Produces("application/rss+xml")
	public String get() throws FeedException {
		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("atom_1.0"); //$NON-NLS-1$
		feed.setTitle(translation.getString("appTitle")); //$NON-NLS-1$
		feed.setDescription(translation.getString("appDescription")); //$NON-NLS-1$
		feed.setLink(translation.getString("baseUrl")); //$NON-NLS-1$
		feed.setUri(resolveUrl());
		
		feed.setEntries(posts.homeList().stream()
			.map(this::toEntry)
			.collect(Collectors.toList()));
		
		return new SyndFeedOutput().outputString(feed);
	}
	
	private SyndEntry toEntry(Post post) {
		SyndEntry entry = new SyndEntryImpl();
		entry.setAuthor(post.getPostedBy());
		entry.setTitle(post.getTitle());
		entry.setPublishedDate(post.getPosted());
		
		List<SyndContent> contents = new ArrayList<>();

		String bodyMarkdown = post.getBodyMarkdown();
		
		
		if(StringUtil.isNotEmpty(bodyMarkdown)) {
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
		read.setHref(resolveUrl(post.getId()));
		SyndLink edit = new SyndLinkImpl();
		edit.setHref(resolveUrl(post.getId()));
		edit.setRel("edit"); //$NON-NLS-1$
		entry.setLinks(Arrays.asList(read, edit));
		
		
		return entry;
	}
	
	private SyndCategory toCategory(String tag) {
		SyndCategory cat = new SyndCategoryImpl();
		cat.setName(tag);
		return cat;
	}
	
	@SneakyThrows
	private String resolveUrl(String... parts) {
		URL url = new URL(servletRequest.getRequestURL().toString());
		String base = new URL(url, servletContext.getContextPath()).toString();
		base = PathUtil.concat(base, BASE_PATH);
		return PathUtil.concat(base, parts);
	}
}
