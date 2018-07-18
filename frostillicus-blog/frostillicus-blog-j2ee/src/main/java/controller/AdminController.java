package controller;

import java.util.ResourceBundle;

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
	public String handlePostLink(
			@PathParam("linkId") String linkId,
			@FormParam("_method") String methodOverride,
			@FormParam("visible") String visible,
			@FormParam("category") String category,
			@FormParam("name") String name,
			@FormParam("url") String url
		) {
		if("DELETE".equals(methodOverride)) {
			return deleteLink(linkId);
		} else {
			return update(linkId, visible, category, name, url);
		}
	}
	
	public String update(String linkId, String visible, String category, String name, String url) {
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
}
