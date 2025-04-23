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
package controller;

import java.util.UUID;

import bean.UserInfoBean;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import model.AccessToken;
import model.Link;

@Controller
@Path("/admin")
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
@RequestScoped
public class AdminController {
	@Inject
	Link.LinkRepository links;
	@Inject
	AccessToken.AccessTokenRepository tokens;
	
	@Inject
	private SecurityContext securityContext;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String show() {
		return "admin.jsp"; //$NON-NLS-1$
	}

	// *******************************************************************************
	// * Links
	// *******************************************************************************

	@POST
	@Path("links/{linkId}")
	public String update(
			@PathParam("linkId") final String linkId,
			@FormParam("visible") final String visible,
			@FormParam("category") final String category,
			@FormParam("name") final String name,
			@FormParam("url") final String url,
			@FormParam("rel") final String rel
		) {
		var link = links.findById(linkId).orElseThrow(() -> new NotFoundException("Unable to find link matching ID " + linkId)); //$NON-NLS-1$
		var newLink = new Link(linkId, category, url, name, "Y".equals(visible), rel, link.classes());
		links.save(newLink);
		return "redirect:admin"; //$NON-NLS-1$
	}

	@DELETE
	@Path("links/{linkId}")
	public String deleteLink(@PathParam("linkId") final String linkId) {
		links.deleteById(linkId);
		return "redirect:admin"; //$NON-NLS-1$
	}

	@POST
	@Path("links/new")
	public String createLink() {
		var link = new Link(null, null, "http://...", "New Link", false, null, null);
		links.save(link);
		return "redirect:admin"; //$NON-NLS-1$
	}

	// *******************************************************************************
	// * Access Tokens
	// *******************************************************************************

	@POST
	@Path("tokens/{tokenId}")
	public String updateToken(
			@PathParam("tokenId") final String tokenId,
			@FormParam("userName") final String userName,
			@FormParam("name") final String name,
			@FormParam("token") final String token
		) {
		tokens.findById(tokenId).orElseThrow(() -> new NotFoundException("Unable to find token matching ID " + tokenId)); //$NON-NLS-1$
		var newToken = new AccessToken(tokenId, userName, name, token);
		tokens.save(newToken);
		return "redirect:admin"; //$NON-NLS-1$
	}

	@DELETE
	@Path("tokens/{tokenId}")
	public String deleteToken(@PathParam("tokenId") final String tokenId) {
		tokens.deleteById(tokenId);
		return "redirect:admin"; //$NON-NLS-1$
	}

	@POST
	@Path("tokens/new")
	public String createToken() {
		var token = new AccessToken(null, securityContext.getUserPrincipal().getName(), "New Token", UUID.randomUUID().toString());
		tokens.save(token);
		return "redirect:admin"; //$NON-NLS-1$
	}

	// *******************************************************************************
	// * Admin console
	// *******************************************************************************

	@GET
	@Path("console")
	@Produces(MediaType.TEXT_HTML)
	public String showConsole() {
		return "admin-console.jsp"; //$NON-NLS-1$
	}
}
