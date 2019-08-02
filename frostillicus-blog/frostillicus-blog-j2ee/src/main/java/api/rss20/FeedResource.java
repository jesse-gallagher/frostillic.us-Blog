/**
 * Copyright © 2012-2019 Jesse Gallagher
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

import java.sql.Date;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.ldap.LdapName;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.w3c.dom.Document;

import com.darwino.commons.util.PathUtil;
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
			baseUrl = translation.getString("baseUrl"); //$NON-NLS-1$
		}
		
		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("rss_2.0"); //$NON-NLS-1$
		feed.setTitle(translation.getString("appTitle")); //$NON-NLS-1$
		feed.setDescription(translation.getString("appDescription")); //$NON-NLS-1$
		feed.setLink(baseUrl);
		
		SyndImage icon = new SyndImageImpl();
		icon.setUrl(PathUtil.concat(baseUrl, servletContext.getContextPath(), "img/icon.png")); //$NON-NLS-1$
		icon.setTitle(translation.getString("appTitle")); //$NON-NLS-1$
		icon.setLink(baseUrl);
		feed.setIcon(icon);
		feed.setImage(icon);
		
		feed.setEntries(posts.homeList().stream()
			.map(post -> toEntry(post, baseUrl))
			.collect(Collectors.toList()));
		
		Document result = new SyndFeedOutput().outputW3CDom(feed);
		result.getDocumentElement().setAttribute("xml:base", baseUrl); //$NON-NLS-1$
		return out -> DomUtil.serialize(out, result, false, true);
	}
	
	@SneakyThrows
	private SyndEntry toEntry(Post post, String baseUrl) {
		SyndEntry entry = new SyndEntryImpl();
		
		String author = post.getPostedBy();
		if(author != null && author.startsWith("cn=")) { //$NON-NLS-1$
			LdapName name = new LdapName(author);
			for(int i = name.size()-1; i >= 0; i--) {
				String bit = name.get(i);
				if(bit.startsWith("cn=")) { //$NON-NLS-1$
					author = bit.substring(3);
				}
			}
		}
		entry.setAuthor(author);
		
		entry.setTitle(post.getTitle());
		entry.setLink(PathUtil.concat(baseUrl, servletContext.getContextPath(), "posts") + "/" + post.getPostedYear() + "/" + post.getPostedMonth() + "/" + post.getPostedDay() + "/" + post.getSlug()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		entry.setPublishedDate(Date.from(post.getPosted().toInstant()));
		SyndContent content = new SyndContentImpl();
		content.setType(MediaType.TEXT_HTML);
		// TODO consider parsing and manipulating the HTML to have a base for images
		content.setValue(post.getBodyHtml());
		entry.setContents(Arrays.asList(content));
		
		return entry;
	}
}
