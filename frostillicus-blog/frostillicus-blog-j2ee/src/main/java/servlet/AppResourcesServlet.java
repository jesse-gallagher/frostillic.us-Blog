package servlet;

import com.darwino.j2ee.servlet.resources.DarwinoAppResourcesServlet;

import jakarta.servlet.annotation.WebServlet;

@WebServlet(name = "FilesServlet", urlPatterns = {"/css/*", "/img/*", "/webjars/*"})
public class AppResourcesServlet extends DarwinoAppResourcesServlet {

}
