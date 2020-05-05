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

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.mvc.Controller;
import javax.mvc.Models;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import bean.UserInfoBean;
import model.MicroPostRepository;

@Path(MicroPostController.PATH)
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
@Controller
@RequestScoped
public class MicroPostController {
	public static final String PATH = "/microposts"; //$NON-NLS-1$
	
	@Inject Models models;
	@Inject MicroPostRepository microPosts;
	
	@GET
	public String list() {
		models.put("posts", microPosts.findAll()); //$NON-NLS-1$
		
		return "microposts.jsp"; //$NON-NLS-1$
	}
	
	@GET
	@Path("{postId}")
	public String show(@PathParam("postId") String postId) {
		models.put("posts", microPosts.findAll()); //$NON-NLS-1$
		
		return "microposts.jsp"; //$NON-NLS-1$
	}
}
