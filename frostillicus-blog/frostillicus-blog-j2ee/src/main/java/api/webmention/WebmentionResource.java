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
package api.webmention;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.jsoup.Jsoup;

import com.darwino.commons.util.PathUtil;
import com.darwino.platform.DarwinoContext;

import app.AsyncManager;
import controller.PostController;
import darwino.AppDatabaseDef;
import model.PostRepository;
import model.Webmention;
import model.Webmention.Type;
import model.WebmentionRepository;

/**
 * Resource to handle Webmention requests.
 *
 * @author Jesse Gallagher
 * @since 2.3.0
 * @see <a href="https://indieweb.org/webmention-spec">https://indieweb.org/webmention-spec</a>
 */
@Path(WebmentionResource.PATH)
public class WebmentionResource {
	public static final String PATH = "webmention"; //$NON-NLS-1$

	private static final Pattern POSTS_MATCHER;
	static {
		try {
		POSTS_MATCHER = Pattern.compile("^" + Pattern.quote(PostController.PATH) + "/\\d{4}/\\d\\d?/\\d\\d?/([^/]+)$"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch(Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	@Context
	UriInfo uriInfo;

	@Inject
	PostRepository posts;
	@Inject
	WebmentionRepository webmentions;
	@Inject
	Logger log;

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response mention(
		@FormParam("source") @NotEmpty final String source,
		@FormParam("target") @NotEmpty final String target,
		@Context final HttpServletRequest request
	) {
		// Assume that the incoming URI info must match the target
		var base = uriInfo.getBaseUri().toString();
		if(!target.startsWith(base)) {
			return error("Target URI must begin with " + base); //$NON-NLS-1$
		}

		var path = PathUtil.concat("/", target.substring(base.length())); //$NON-NLS-1$
		var matcher = POSTS_MATCHER.matcher(path);
		if(matcher.matches()) {
			var postId = matcher.group(1);
			var post = posts.findPost(postId);
			if(!post.isPresent()) {
				return error("Unable to find post for ID " + postId); //$NON-NLS-1$
			}

			var webmention = webmentions.find(source, Type.Post.name(), post.get().getPostId()).orElseGet(() -> {
				Webmention result = new Webmention();
				result.setSource(source);
				result.setType(Type.Post);
				result.setTargetId(post.get().getPostId());
				result.setPosted(OffsetDateTime.now());

				result.setHttpReferer(request.getHeader("Referer")); //$NON-NLS-1$
				result.setHttpRemoteAddr(request.getRemoteAddr());
				result.setHttpUserAgent(request.getHeader("User-Agent")); //$NON-NLS-1$

				return webmentions.save(result);
			});

			AsyncManager.executor.submit(() -> {
				try {
					// Use the Darwino API to avoid an NPE in LiberyValidatorProxy on WS Liberty
					var database = DarwinoContext.get().getSession().getDatabase(AppDatabaseDef.DATABASE_NAME);
					var mentions = database.getStore(AppDatabaseDef.STORE_WEBMENTIONS);
					var mention = mentions.loadDocument(webmention.getId());

					try {
						var doc = Jsoup.connect(source).get();

						// Check to make sure that the target is indeed present
						var isReferenced = doc.select("a[href]").parallelStream() //$NON-NLS-1$
							.map(a -> a.attr("href")) //$NON-NLS-1$
							.map(String::valueOf)
							.anyMatch(href -> href.startsWith(target));
						if(!isReferenced) {
							// Then it's not just applicable
							if(log.isLoggable(Level.SEVERE)) {
								log.severe("Skipping Webmention without associated link: " + source); //$NON-NLS-1$
							}
							mention.set("verified", 0); //$NON-NLS-1$
						} else {
							mention.set("verified", 1); //$NON-NLS-1$
							mention.set("sourceTitle", doc.title()); //$NON-NLS-1$
						}
					} catch(IOException e) {
						if(log.isLoggable(Level.SEVERE)) {
							log.log(Level.SEVERE, "Encountered exception when looking up webmention", e); //$NON-NLS-1$
						}
						mention.set("verified", 0); //$NON-NLS-1$
						mention.set("problemCause", e.toString()); //$NON-NLS-1$
					}

					mention.save();
				} catch (Throwable t) {
					t.printStackTrace();
					if(log.isLoggable(Level.SEVERE)) {
						log.log(Level.SEVERE, "Exception when processing Webmention", t); //$NON-NLS-1$
					}
				}
			});

			return Response.accepted().build();
		} else {
			return error("Unable to match path to entity: " + path); //$NON-NLS-1$
		}
	}

	private Response error(final String reason) {
		return Response
			.status(Status.BAD_REQUEST)
			.type(MediaType.TEXT_PLAIN_TYPE)
			.entity(reason)
			.build();
	}
}
