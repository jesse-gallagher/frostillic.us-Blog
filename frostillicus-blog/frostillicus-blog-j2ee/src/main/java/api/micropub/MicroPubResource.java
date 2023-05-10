/*
 * Copyright © 2012-2023 Jesse Gallagher
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
package api.micropub;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.darwino.commons.util.PathUtil;
import com.darwino.commons.util.StringUtil;

import api.micropub.MicroPubClient.EntryType;
import api.rsd.RSD;
import api.rsd.RSDService;
import bean.UserInfoBean;
import controller.MicroPostController;
import darwino.AppDatabaseDef;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
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
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
@RequestScoped
public class MicroPubResource implements RSD {
	public static final String BASE_PATH = "/micropub"; //$NON-NLS-1$

	public enum EntityAction {
		delete, undelete
	}

	@Inject @Named("translation")
	ResourceBundle translation;
	@Context
	ServletContext servletContext;
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	@Inject
	MicroPostRepository microPosts;

	@Inject
	@ConfigProperty(name=AppDatabaseDef.DATABASE_NAME+".rss-request-urls", defaultValue="false")
	private boolean rssRequestUrls;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public void get(@QueryParam("q") final String info) {
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
		@FormParam("h") final EntryType entityType,
		@FormParam("name") final String name,
		@FormParam("content") final String content,
		@FormParam("category") final String category,
		@FormParam("category[]") final List<String> categories
	) throws IOException {
		return Response.created(create(entityType, name, content, category, categories)).build();
	}

//	@POST
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	public Response createFormData(
//		@FormParam("h") final EntryType entityType,
//		@FormParam("name") final String name,
//		@FormParam("content") final String content,
//		@FormParam("category") final String category,
//		@FormParam("category[]") final List<String> categories,
//		@FormParam("file") final BodyPart image,
//		@HeaderParam("Accept") final String accept
//	) {
//		var uri = create(entityType, name, content, category, categories);
//		var builder = Response.created(uri);
//		if(StringUtil.isNotEmpty(accept) && accept.startsWith("text/html")) { //$NON-NLS-1$
//			// Special support for browsers
//			builder.header("Refresh", "0; url=" + uri); //$NON-NLS-1$ //$NON-NLS-2$
//		}
//		return builder.build();
//	}

	private URI create(final EntryType entityType, final String name, final String content, final String category, final List<String> categories) {
		switch(entityType) {
		case entry:
			var microPost = new MicroPost();
			microPost.setName(name);
			microPost.setContent(content);
			microPost = microPosts.save(microPost);

			String baseUrl;
			if(rssRequestUrls) {
				baseUrl = uriInfo.getBaseUri().toString();
			} else {
				baseUrl = PathUtil.concat(translation.getString("baseUrl"), servletContext.getContextPath()); //$NON-NLS-1$
			}

			return URI.create(PathUtil.concat(baseUrl, MicroPostController.PATH, microPost.getPostId()));
		}
		throw new IllegalArgumentException("Unable to create entity of type " + entityType);
	}
}
