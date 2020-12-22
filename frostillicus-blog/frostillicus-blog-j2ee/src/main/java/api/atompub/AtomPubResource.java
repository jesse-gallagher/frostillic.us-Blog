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

import api.rsd.RSDService;
import bean.UserInfoBean;

@Path(AtomPubResource.BASE_PATH)
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
@RSDService(name="AtomPub", basePath=AtomPubResource.BASE_PATH, preferred=false)
public class AtomPubResource {
	public static final String BASE_PATH = "atompub"; //$NON-NLS-1$

	public static final String BLOG_ID = "";

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
		return null;
	}

	private String resolveUrl(final String... parts) {
		return null;
	}
}
