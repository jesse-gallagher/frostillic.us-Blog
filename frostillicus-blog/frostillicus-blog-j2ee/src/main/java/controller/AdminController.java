package controller;

import java.util.ResourceBundle;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mvc.annotation.Controller;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import model.Link;
import model.LinkRepository;

@Controller
@Path("admin")
@RolesAllowed("admin")
public class AdminController {
	@Inject
	LinkRepository links;
	@Inject @Named("translation")
	ResourceBundle translation;
	
	@GET
	public String show() {
		return "admin.jsp";
	}
	
	@POST
	@Path("links/{linkId}")
	public String update(
			@PathParam("linkId") String linkId,
			@FormParam("visible") String visible,
			@FormParam("category") String category,
			@FormParam("name") String name,
			@FormParam("url") String url
		) {
		Link link = links.findById(linkId).orElseThrow(() -> new IllegalArgumentException("Unable to find link matching ID " + linkId));
		link.setVisible("Y".equals(visible));
		link.setCategory(category);
		link.setName(name);
		link.setUrl(url);
		links.save(link);
		return "redirect:admin";
	}
	
	@DELETE
	@Path("links/{linkId}")
	public String deleteLink(@PathParam("linkId") String linkId) {
		links.deleteById(linkId);
		return "redirect:admin";
	}
	
	@POST
	@Path("links/new")
	public String createLink() {
		Link link = new Link();
		link.setName("New Link");
		link.setUrl("http://...");
		links.save(link);
		return "redirect:admin";
	}
}
