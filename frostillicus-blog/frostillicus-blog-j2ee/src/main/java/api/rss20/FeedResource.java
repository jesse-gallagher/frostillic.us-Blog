/*
 * Copyright (c) 2012-2023 Jesse Gallagher
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

import java.util.ResourceBundle;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.darwino.commons.util.PathUtil;
import com.darwino.commons.util.StringUtil;

import api.rss20.model.AtomLink;
import api.rss20.model.Channel;
import api.rss20.model.Image;
import api.rss20.model.Rss;
import api.rss20.model.RssItem;
import bean.UrlBean;
import darwino.AppDatabaseDef;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
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
	UrlBean urlBean;

	@Inject
	@ConfigProperty(name=AppDatabaseDef.DATABASE_NAME+".rss-request-urls", defaultValue="false")
	private boolean rssRequestUrls;

	@GET
	@Produces("application/rss+xml")
	public Rss get() {
		String baseUrl;
		if(rssRequestUrls) {
			baseUrl = uriInfo.getBaseUri().toString();
		} else {
			baseUrl = PathUtil.concat(translation.getString("baseUrl"), servletContext.getContextPath()); //$NON-NLS-1$
		}

		Rss rss = new Rss();
		rss.setBase(baseUrl);
		Channel channel = rss.getChannel();
		channel.setTitle(translation.getString("appTitle")); //$NON-NLS-1$
		channel.setDescription(translation.getString("appDescription")); //$NON-NLS-1$
		channel.setLink(baseUrl);

		AtomLink self = new AtomLink();
		self.setRel("self"); //$NON-NLS-1$
		self.setType("application/rss+xml"); //$NON-NLS-1$
		self.setHref(urlBean.concat(baseUrl, "blog.xml")); //$NON-NLS-1$
		channel.getLinks().add(self);

		Image image = channel.getImage();
		image.setUrl(urlBean.concat(baseUrl, "img/icon.png")); //$NON-NLS-1$
		image.setTitle(translation.getString("appTitle")); //$NON-NLS-1$
		image.setLink(baseUrl);

		posts.homeList().stream()
			.map(post -> toEntry(post, baseUrl))
			.forEach(channel.getItems()::add);

		return rss;
	}

	@SneakyThrows
	private RssItem toEntry(final Post post, final String baseUrl) {
		RssItem entry = new RssItem();

		var author = PostUtil.toCn(post.getPostedBy());
		entry.setCreator(author);

		entry.setTitle(post.getTitle());
		entry.setLink(PathUtil.concat(baseUrl, "posts") + "/" + post.getPostedYear() + "/" + post.getPostedMonth() + "/" + post.getPostedDay() + "/" + post.getSlug()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		entry.setDate(post.getPosted().toInstant());
		entry.setGuid(post.getId());

		var summary = post.getSummary();
		String content;
		if(StringUtil.isNotEmpty(summary)) {
			content = summary;
		} else {
			content = post.getBodyHtml();
		}
		entry.setContentEncoded(content);

		return entry;
	}
}
