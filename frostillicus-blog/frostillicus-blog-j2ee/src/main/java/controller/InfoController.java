/**
 * Copyright © 2016-2019 Jesse Gallagher
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

import bean.DominoBean;
import bean.UserInfoBean;

import javax.annotation.security.RolesAllowed;
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
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
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
        info.put("Remote IP", req.getRemoteHost());

        models.put("info", info);

        return "info.jsp";
    }
}
