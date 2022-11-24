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
package api.rss20;

import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.darwino.commons.util.PathUtil;
import com.darwino.commons.util.StringUtil;
import com.darwino.commons.xml.DomUtil;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.feed.synd.SyndImageImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

import darwino.AppDatabaseDef;
import lombok.SneakyThrows;
import model.Post;
import model.PostRepository;
import model.util.PostUtil;

@Path("/feed.xml")
public class FeedResource {
	@Inject
	PostRepository posts;
	@Inject @Named("translation")
	ResourceBundle translation;
	@Context
	ServletContext servletContext;
	@Context
	UriInfo uriInfo;

	@Inject
	@ConfigProperty(name=AppDatabaseDef.DATABASE_NAME+".rss-request-urls", defaultValue="false")
	private boolean rssRequestUrls;

	@GET
	@Produces("application/rss+xml")
	public StreamingOutput get() throws FeedException {
		String baseUrl;
		if(rssRequestUrls) {
			baseUrl = uriInfo.getBaseUri().toString();
		} else {
			baseUrl = PathUtil.concat(translation.getString("baseUrl"), servletContext.getContextPath()); //$NON-NLS-1$
		}

		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("rss_2.0"); //$NON-NLS-1$
		feed.setTitle(translation.getString("appTitle")); //$NON-NLS-1$
		feed.setDescription(translation.getString("appDescription")); //$NON-NLS-1$
		feed.setLink(baseUrl);

		SyndImage icon = new SyndImageImpl();
		icon.setUrl(PathUtil.concat(baseUrl, "img/icon.png")); //$NON-NLS-1$
		icon.setTitle(translation.getString("appTitle")); //$NON-NLS-1$
		icon.setLink(baseUrl);
		feed.setIcon(icon);
		feed.setImage(icon);

		feed.setEntries(posts.homeList().stream()
			.map(post -> toEntry(post, baseUrl))
			.collect(Collectors.toList()));

		var result = new SyndFeedOutput().outputW3CDom(feed);
		result.getDocumentElement().setAttribute("xml:base", baseUrl); //$NON-NLS-1$
		return out -> DomUtil.serialize(out, result, false, true);
	}

	@SneakyThrows
	private SyndEntry toEntry(final Post post, final String baseUrl) {
		SyndEntry entry = new SyndEntryImpl();

		var author = PostUtil.toCn(post.getPostedBy());
		entry.setAuthor(author);

		entry.setTitle(post.getTitle());
		entry.setLink(PathUtil.concat(baseUrl, "posts") + "/" + post.getPostedYear() + "/" + post.getPostedMonth() + "/" + post.getPostedDay() + "/" + post.getSlug()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		entry.setPublishedDate(java.util.Date.from(post.getPosted().toInstant()));

		var summary = post.getSummary();
		SyndContent content = new SyndContentImpl();
		if(StringUtil.isNotEmpty(summary)) {
			content.setType(MediaType.TEXT_PLAIN);
			content.setValue(summary);
		} else {
			content.setType(MediaType.TEXT_HTML);
			// TODO consider parsing and manipulating the HTML to have a base for images
			content.setValue(post.getBodyHtml());
		}
		entry.setContents(Arrays.asList(content));

		return entry;
	}
}
