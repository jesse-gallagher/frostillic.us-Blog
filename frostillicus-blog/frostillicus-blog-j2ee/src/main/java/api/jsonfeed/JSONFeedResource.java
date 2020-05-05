/**
 * Copyright © 2012-2020 Jesse Gallagher
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
package api.jsonfeed;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.annotation.JsonbProperty;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.darwino.commons.util.PathUtil;

import bean.UserInfoBean;
import darwino.AppDatabaseDef;
import lombok.Data;
import model.Post;
import model.PostRepository;

@Path("/feed.json")
public class JSONFeedResource {
	@Data
	public static class Feed {
		String version = "https://jsonfeed.org/version/1"; //$NON-NLS-1$
		String title;
		@JsonbProperty("home_page_url") String homePageUrl;
		@JsonbProperty("feed_url") String feedUrl;
		String description;
		String icon;
		String favicon;
		
		List<FeedItem> items;
	}
	@Data
	public static class FeedItem {
		String id;
		@JsonbProperty("content_html") String contentHtml;
		String url;
		String summary;
		@JsonbProperty("date_published") Date published;
		@JsonbProperty("date_modified") Date modified;
		List<String> tags;
	}

	@Inject
	PostRepository posts;

	@Inject @Named("translation")
	ResourceBundle translation;
	
	@Inject
	UserInfoBean userInfo;

	@Context
	UriInfo uriInfo;

	@Context
	ServletContext servletContext;
	
	@Inject
	@ConfigProperty(name=AppDatabaseDef.DATABASE_NAME+".rss-request-urls", defaultValue="false")
	private boolean rssRequestUrls;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput get() {
		return out -> {
			try(Jsonb jsonb = JsonbBuilder.create()) {
				String baseUrl;
				if(rssRequestUrls) {
					baseUrl = uriInfo.getBaseUri().toString();
				} else {
					baseUrl = PathUtil.concat(translation.getString("baseUrl"), servletContext.getContextPath()); //$NON-NLS-1$
				}
				
				Feed feed = new Feed();
				feed.setTitle(translation.getString("appTitle")); //$NON-NLS-1$
				feed.setFeedUrl(PathUtil.concat(baseUrl, JSONFeedResource.class.getAnnotation(Path.class).value()));
				feed.setHomePageUrl(baseUrl);
				feed.setDescription(translation.getString("appDescription")); //$NON-NLS-1$
				feed.setIcon(PathUtil.concat(baseUrl, userInfo.getImageUrl(translation.getString("authorEmail")))); //$NON-NLS-1$
				feed.setFavicon(PathUtil.concat(baseUrl, "img/icon.png")); //$NON-NLS-1$
				
				feed.setItems(posts.homeList().stream()
						.map(post -> toItem(post, baseUrl))
						.collect(Collectors.toList()));
				
				
				jsonb.toJson(feed, out);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}
	
	private FeedItem toItem(Post post, String baseUrl) {
		FeedItem item = new FeedItem();
		item.setUrl(PathUtil.concat(baseUrl, "posts") + "/" + post.getPostedYear() + "/" + post.getPostedMonth() + "/" + post.getPostedDay() + "/" + post.getSlug()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		item.setId(post.getPostId());
		item.setContentHtml(post.getBodyHtml());
		item.setSummary(post.getSummary());
		item.setPublished(post.getPostedDate());
		item.setModified(Date.from(post.getModified().toInstant()));
		item.setTags(post.getTags());
		return item;
	}
}
