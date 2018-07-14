package controller;

import javax.inject.Inject;
import javax.mvc.Models;
import javax.mvc.annotation.Controller;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("hello")
@Controller
public class HelloController {
	@Inject
	private Models model;
	
	@GET
	public String hello() {
		model.put("hello", "Hello!");
		return "/hello.jsp";
	}
}
