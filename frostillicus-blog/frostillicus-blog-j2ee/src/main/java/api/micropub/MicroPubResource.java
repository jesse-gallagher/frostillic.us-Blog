package api.micropub;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.darwino.commons.util.PathUtil;
import com.darwino.commons.util.StringUtil;
import com.darwino.commons.util.io.StreamUtil;
import com.darwino.platform.DarwinoContext;

import api.rsd.RSDService;
import bean.UserInfoBean;
import controller.MicroPostController;
import darwino.AppDatabaseDef;
import model.MicroPost;
import model.MicroPostRepository;

/**
 * Implements the Micropub API as used by micro.blog.
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@Path(MicroPubResource.BASE_PATH)
@RSDService(name="Micropub", basePath=MicroPubResource.BASE_PATH, preferred=false)
public class MicroPubResource {
	public static final String BASE_PATH = "micropub"; //$NON-NLS-1$
	
	public enum EntityType {
		entry
	}
	public enum EntityAction {
		delete, undelete
	}

	@Inject @Named("translation")
	ResourceBundle translation;
	@Context
	ServletContext servletContext;
	@Context
	UriInfo uriInfo;
	@Inject
	MicroPostRepository microPosts;

	@Inject
	@ConfigProperty(name=AppDatabaseDef.DATABASE_NAME+".rss-request-urls", defaultValue="false")
	private boolean rssRequestUrls;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public void get(@QueryParam("q") String info) {
		checkAuth();
		
		switch(StringUtil.toString(info)) {
		case "q": //$NON-NLS-1$
			break;
		default:
			throw new IllegalArgumentException("Unknown value for q: " + info); //$NON-NLS-1$
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createUrlEncoded(
		@FormParam("h") EntityType entityType,
		@FormParam("content") String content,
		@FormParam("category") String category,
		@FormParam("category[]") List<String> categories,
		InputStream catchall
	) throws IOException {
		checkAuth();
		
		switch(entityType) {
		case entry:
			MicroPost microPost = new MicroPost();
			microPost.setContent(content);
			microPost = microPosts.save(microPost);
			
			String baseUrl;
			if(rssRequestUrls) {
				baseUrl = uriInfo.getBaseUri().toString();
			} else {
				baseUrl = PathUtil.concat(translation.getString("baseUrl"), servletContext.getContextPath()); //$NON-NLS-1$
			}
			
			return Response.created(URI.create(PathUtil.concat(baseUrl, MicroPostController.PATH, microPost.getPostId()))).build();
		}
		return Response.noContent().build();
	}
	
	// Can't use @RolesAllowed here yet, since that is enforced by the container and not Darwino,
	//   and so doesn't work with the Darwino AuthHandler. I'm not sure why my custom one doesn't
	//   work the same way that Basic and form auth do, though.
	private void checkAuth() {
		if(!DarwinoContext.get().getUser().getRoles().contains(UserInfoBean.ROLE_ADMIN)) {
			throw new NotAuthorizedException(Response.status(Response.Status.UNAUTHORIZED).build());
		}
	}
}
