package controller;

import javax.mvc.annotation.Controller;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
@Controller
public class HomeController {
	@GET
	public String get() {
		return "home.jsp";
	}
}
