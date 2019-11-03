package api.akismet;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * MicroProfile REST Client representation of the Akismet 1.1 API.
 *  
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@Path("/1.1")
public interface Akismet11Client {
	public static final String BASE_HOST = "rest.akismet.com"; //$NON-NLS-1$
	
	@Path("verify-key")
	@POST
	public String verifyKey(
		@FormParam("key") String key,
		@FormParam("blog") String blog
	);
	
	@Path("check-comment")
	@POST
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
}
