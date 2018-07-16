package controller;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Inject;
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
public class FeedController {
	@Inject
	PostRepository posts;
	@Context
	ServletContext servletContext;
	
	@GET
	@Produces("application/rss+xml")
	public String get() throws FeedException {
		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("rss_2.0");
		feed.setTitle("frostillic.us");
		feed.setDescription("frostillic.us");
		feed.setLink("https://frostillic.us");
		
		feed.setEntries(posts.homeList().stream()
			.map(this::toEntry)
			.collect(Collectors.toList()));
		
		return new SyndFeedOutput().outputString(feed);
	}
	
	private SyndEntry toEntry(Post post) {
		SyndEntry entry = new SyndEntryImpl();
		entry.setAuthor(post.getPostedBy());
		entry.setTitle(post.getTitle());
		entry.setLink("https://frostillic.us" + servletContext.getContextPath() + "/posts/" + post.getId());
		entry.setPublishedDate(post.getPosted());
		SyndContent content = new SyndContentImpl();
		content.setType(MediaType.TEXT_HTML);
		content.setValue(post.getBodyHtml());
		entry.setContents(Arrays.asList(content));
		
		return entry;
	}
}
