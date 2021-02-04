/*
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
package api.atompub;

import java.util.ResourceBundle;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.darwino.commons.util.PathUtil;
import com.darwino.commons.xml.DomUtil;

import api.rsd.RSDService;
import bean.UserInfoBean;
import darwino.AppDatabaseDef;

@Path(AtomPubResource.BASE_PATH)
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
@RSDService(name="AtomPub", basePath=AtomPubResource.BASE_PATH, preferred=false)
public class AtomPubResource {
	public static final String BASE_PATH = "/atompub"; //$NON-NLS-1$

	public static final String BLOG_ID = AppDatabaseDef.DATABASE_NAME;

	@Inject @Named("translation")
	ResourceBundle translation;
	@Context
	ServletContext servletContext;
	@Context
	HttpServletRequest servletRequest;
	@Context
	UriInfo uriInfo;

	// This only supports the one active blog
	@GET
	@Produces("application/atomserv+xml")
	public String getWorkspace() {
		var xml = DomUtil.createDocument();
		var service = DomUtil.createRootElement(xml, "service"); //$NON-NLS-1$
		service.setAttribute("xmlns", "http://purl.org/atom/app#"); //$NON-NLS-1$ //$NON-NLS-2$
		service.setAttribute("xmlns:atom", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$

		var workspace = DomUtil.createElement(service, "workspace"); //$NON-NLS-1$
		DomUtil.createElement(workspace, "atom:title", BLOG_ID); //$NON-NLS-1$

		// Blog posts collection
		{
			var collection = DomUtil.createElement(workspace, "collection"); //$NON-NLS-1$
			collection.setAttribute("href", resolveUrl(BLOG_ID)); //$NON-NLS-1$
			DomUtil.createElement(collection, "atom:title", "Entries"); //$NON-NLS-1$ //$NON-NLS-2$
			var categories = DomUtil.createElement(collection, "categories"); //$NON-NLS-1$
			categories.setAttribute("href", resolveUrl(BLOG_ID, "categories")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Media collection
		{
			var collection = DomUtil.createElement(workspace, "collection"); //$NON-NLS-1$
			collection.setAttribute("href", resolveUrl(BLOG_ID, MediaResource.PATH)); //$NON-NLS-1$
			DomUtil.createElement(collection, "atom:title", "Pictures"); //$NON-NLS-1$ //$NON-NLS-2$
			Stream.of(
				"image/png", //$NON-NLS-1$
				"image/jpeg", //$NON-NLS-1$
				"image/gif", //$NON-NLS-1$
				"image/webp" //$NON-NLS-1$
			).forEach(type -> DomUtil.createElement(collection, "accept", type)); //$NON-NLS-1$
		}

		return DomUtil.getXMLString(xml);
	}

	private String resolveUrl(final String... parts) {
		var baseUri = uriInfo.getBaseUri();
		var uri = PathUtil.concat(baseUri.toString(), BASE_PATH);
		for(String part : parts) {
			uri = PathUtil.concat(uri, part);
		}
		return uri;
	}
}
