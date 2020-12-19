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
package controller;

import bean.UserInfoBean;
import com.darwino.commons.json.JsonException;
import com.darwino.jsonstore.Session;
import model.AccessToken;
import model.AccessTokenRepository;
import model.Link;
import model.LinkRepository;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.mvc.Controller;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Controller
@Path("admin")
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
@RequestScoped
public class AdminController {
	@Inject
	LinkRepository links;
	@Inject
	AccessTokenRepository tokens;
	@Inject
	Session session;

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
		link.setVisible("Y".equals(visible)); //$NON-NLS-1$
		link.setCategory(category);
		link.setName(name);
		link.setUrl(url);
		link.setRel(rel);
		links.save(link);
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
		var link = new Link();
		link.setName("New Link"); //$NON-NLS-1$
		link.setUrl("http://..."); //$NON-NLS-1$
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
		var accessToken = tokens.findById(tokenId).orElseThrow(() -> new NotFoundException("Unable to find token matching ID " + tokenId)); //$NON-NLS-1$
		accessToken.setUserName(userName);
		accessToken.setName(name);
		accessToken.setToken(token);
		tokens.save(accessToken);
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
	public String createToken() throws JsonException {
		var token = new AccessToken();
		token.setUserName(session.getUser().getDn());
		token.setName("New Token"); //$NON-NLS-1$
		token.setToken(UUID.randomUUID().toString());
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
