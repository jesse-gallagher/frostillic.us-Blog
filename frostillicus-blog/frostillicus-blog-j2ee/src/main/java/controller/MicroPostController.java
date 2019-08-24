/**
 * Copyright © 2012-2019 Jesse Gallagher
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
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import bean.UserInfoBean;
import model.MicroPost;
import model.MicroPostRepository;

@Path("/microposts")
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
@Controller
@RequestScoped
public class MicroPostController {
	
	@Inject Models models;
	@Inject MicroPostRepository microPosts;
	
	@GET
	public String list() {
		models.put("posts", microPosts.findAll()); //$NON-NLS-1$
		
		return "microposts.jsp"; //$NON-NLS-1$
	}
	
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED })
	public String create(@FormParam("name") String name, @FormParam("content") String content) {
		MicroPost microPost = new MicroPost();
		microPost.setName(name);
		microPost.setContent(content);
		microPosts.save(microPost);
		
		return "redirect:microposts"; //$NON-NLS-1$
	}
}
