/*
 * Copyright © 2012-2023 Jesse Gallagher
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

import static model.util.PostUtil.PAGE_LENGTH;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.StringUtil;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/")
@Controller
@RequestScoped
public class HomeController extends AbstractPostListController {

	@Inject PostController postController;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String get(@QueryParam("start") final String startParam, @QueryParam("p") final String postId) throws JsonException {
		if(StringUtil.isNotEmpty(postId)) {
			// Support for very-old-style "?p=foo" URLs
			return postController.show(postId);
		}

		var maybeList = maybeList(startParam);
		if(StringUtil.isEmpty(maybeList)) {
    		var homeList = posts.homeList();
			models.put("posts", homeList); //$NON-NLS-1$
			models.put("start", 0); //$NON-NLS-1$
		}
		models.put("pageSize", PAGE_LENGTH); //$NON-NLS-1$

		return "home.jsp"; //$NON-NLS-1$
	}
}