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

import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.StringUtil;

import bean.UserInfoBean;
import model.Post;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.mvc.Controller;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import static model.util.PostUtil.PAGE_LENGTH;

import java.util.List;

@Path("/")
@Controller
@RequestScoped
public class HomeController extends AbstractPostListController {
	
	@Inject
	UserInfoBean userInfo;
	
	@GET
	public String get(@QueryParam("start") String startParam) throws JsonException {
		String maybeList = maybeList(startParam);
		if(StringUtil.isEmpty(maybeList)) {
    		List<Post> homeList = userInfo.isAdmin() ? posts.homeListAdmin() : posts.homeList();
			models.put("posts", homeList); //$NON-NLS-1$
			models.put("start", 0); //$NON-NLS-1$
		}
		models.put("pageSize", PAGE_LENGTH); //$NON-NLS-1$
		
		return "home.jsp"; //$NON-NLS-1$
	}
}