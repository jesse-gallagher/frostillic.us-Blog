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
package api.webmention;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewQuery;

import com.ibm.commons.util.PathUtil;

import controller.PostController;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import model.Post;
import model.Webmention;
import model.Webmention.Type;

/**
 * Resource to handle Webmention requests.
 *
 * @author Jesse Gallagher
 * @since 2.3.0
 * @see <a href="https://indieweb.org/webmention-spec">https://indieweb.org/webmention-spec</a>
 */
@Path(WebmentionResource.PATH)
public class WebmentionResource {
	public static final String PATH = "/webmention"; //$NON-NLS-1$

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
	Post.PostRepository posts;
	@Inject
	Webmention.WebmentionRepository webmentions;
	@Inject
	Logger log;

	@Inject @Named("java:comp/DefaultManagedExecutorService")
	private ManagedExecutorService exec;

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

		var path = PathUtil.concat("/", target.substring(base.length()), '/'); //$NON-NLS-1$
		var matcher = POSTS_MATCHER.matcher(path);
		if(matcher.matches()) {
			var postId = matcher.group(1);
			var post = posts.findByPostId(ViewQuery.query().key(postId, true));
			if(!post.isPresent()) {
				return error("Unable to find post for ID " + postId); //$NON-NLS-1$
			}

			var mention = webmentions.findBySourceAndTypeAndTargetId(source, Type.Post.name(), post.get().getPostId()).orElseGet(() -> {
				var result = new Webmention();
				result.setSource(source);
				result.setType(Type.Post);
				result.setTargetId(post.get().getPostId());
				result.setPosted(OffsetDateTime.now());

				result.setHttpReferer(request.getHeader("Referer")); //$NON-NLS-1$
				result.setHttpRemoteAddr(request.getRemoteAddr());
				result.setHttpUserAgent(request.getHeader("User-Agent")); //$NON-NLS-1$

				return webmentions.save(result);
			});

			exec.submit(() -> {
				try {
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
							mention.setVerified(false);
						} else {
							mention.setVerified(true);
							mention.setSourceTitle(doc.title());
						}
					} catch(IOException e) {
						if(log.isLoggable(Level.SEVERE)) {
							log.log(Level.SEVERE, "Encountered exception when looking up webmention", e); //$NON-NLS-1$
						}
						mention.setVerified(false);
						mention.setProblemCause(e.toString());
					}

					webmentions.save(mention);
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
