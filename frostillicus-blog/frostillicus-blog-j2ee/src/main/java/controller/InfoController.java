package controller;

import bean.DominoBean;

import javax.inject.Inject;
import javax.mvc.Controller;
import javax.mvc.Models;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Path("info")
@Controller
public class InfoController {
    @Inject
    Models models;

    @Inject
    DominoBean dominoInfo;

    @Inject
    HttpServletRequest req;

    @GET
    public String get() throws ExecutionException, InterruptedException {
        Map<Object, Object> info = new LinkedHashMap<>();

        info.put("Domino Version", dominoInfo.getVersion());
        info.put("Java VM", System.getProperty("java.vm.info"));
        info.put("Servlet Version", req.getServletContext().getMajorVersion() + "." + req.getServletContext().getMinorVersion());
        info.put("Protocol", req.getProtocol());
        info.put("Server Info", req.getServletContext().getServerInfo());
        info.put("Server Name", dominoInfo.getServerName());

        models.put("info", info);

        return "info.jsp";
    }
}
