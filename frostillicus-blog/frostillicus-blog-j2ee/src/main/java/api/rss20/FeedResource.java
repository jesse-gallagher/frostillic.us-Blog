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
package api.rss20;

import java.sql.Date;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

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
	
	@GET
	@Produces("application/rss+xml")
	public String get() throws FeedException {
		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("rss_2.0"); //$NON-NLS-1$
		feed.setTitle(translation.getString("appTitle")); //$NON-NLS-1$
		feed.setDescription(translation.getString("appDescription")); //$NON-NLS-1$
		feed.setLink(translation.getString("baseUrl")); //$NON-NLS-1$
		
		feed.setEntries(posts.homeList().stream()
			.map(this::toEntry)
			.collect(Collectors.toList()));
		
		return new SyndFeedOutput().outputString(feed);
	}
	
	private SyndEntry toEntry(Post post) {
		SyndEntry entry = new SyndEntryImpl();
		entry.setAuthor(post.getPostedBy());
		entry.setTitle(post.getTitle());
		entry.setLink(translation.getString("baseUrl") + servletContext.getContextPath() + "/posts/" + post.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		entry.setPublishedDate(Date.from(post.getPosted().toInstant()));
		SyndContent content = new SyndContentImpl();
		content.setType(MediaType.TEXT_HTML);
		content.setValue(post.getBodyHtml());
		entry.setContents(Arrays.asList(content));
		
		return entry;
	}
}
