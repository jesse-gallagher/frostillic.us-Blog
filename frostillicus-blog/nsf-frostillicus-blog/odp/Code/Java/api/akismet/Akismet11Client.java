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
package api.akismet;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * MicroProfile REST Client representation of the Akismet 1.1 API.
 *
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@Path("/1.1")
public interface Akismet11Client extends AutoCloseable {
	public static final String BASE_HOST = "rest.akismet.com"; //$NON-NLS-1$

	@Path("verify-key")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public String verifyKey(
		@FormParam("key") String key,
		@FormParam("blog") String blog
	);

	@Path("comment-check")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public boolean checkComment(
		@FormParam("blog") String blog,
		@FormParam("user_ip") String remoteAddress,
		@FormParam("user_agent") String userAgent,
		@FormParam("referrer") String referrer,
		@FormParam("permalink") String permalink,
		@FormParam("comment_type") String commentType,
		@FormParam("comment_author") String author,
		@FormParam("comment_author_email") String authorEmail,
		@FormParam("comment_author_url") String authorUrl,
		@FormParam("comment_content") String content
	);
	
	@Override
	void close();
}
