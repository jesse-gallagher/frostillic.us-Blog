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
package controller;

import java.util.ResourceBundle;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mvc.Controller;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.darwino.commons.json.JsonException;
import com.darwino.jsonstore.Session;

import bean.UserInfoBean;
import model.AccessToken;
import model.AccessTokenRepository;
import model.Link;
import model.LinkRepository;

@Controller
@Path("admin")
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
@RequestScoped
public class AdminController {
	@Inject
	LinkRepository links;
	@Inject
	AccessTokenRepository tokens;
	@Inject @Named("translation")
	ResourceBundle translation;
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
			@PathParam("linkId") String linkId,
			@FormParam("visible") String visible,
			@FormParam("category") String category,
			@FormParam("name") String name,
			@FormParam("url") String url,
			@FormParam("rel") String rel
		) {
		Link link = links.findById(linkId).orElseThrow(() -> new NotFoundException("Unable to find link matching ID " + linkId)); //$NON-NLS-1$
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
	public String deleteLink(@PathParam("linkId") String linkId) {
		links.deleteById(linkId);
		return "redirect:admin"; //$NON-NLS-1$
	}
	
	@POST
	@Path("links/new")
	public String createLink() {
		Link link = new Link();
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
			@PathParam("tokenId") String tokenId,
			@FormParam("userName") String userName,
			@FormParam("name") String name,
			@FormParam("token") String token
		) {
		AccessToken accessToken = tokens.findById(tokenId).orElseThrow(() -> new NotFoundException("Unable to find token matching ID " + tokenId)); //$NON-NLS-1$
		accessToken.setUserName(userName);
		accessToken.setName(name);
		accessToken.setToken(token);
		tokens.save(accessToken);
		return "redirect:admin"; //$NON-NLS-1$
	}
	
	@DELETE
	@Path("tokens/{tokenId}")
	public String deleteToken(@PathParam("tokenId") String tokenId) {
		tokens.deleteById(tokenId);
		return "redirect:admin"; //$NON-NLS-1$
	}
	
	@POST
	@Path("tokens/new")
	public String createToken() throws JsonException {
		AccessToken token = new AccessToken();
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
