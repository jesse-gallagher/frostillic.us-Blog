/**
 * Copyright © 2016-2018 Jesse Gallagher
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

import javax.inject.Inject;
import javax.mvc.Models;
import javax.mvc.Controller;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.StringUtil;
import com.darwino.jsonstore.Database;
import model.PostRepository;
import model.PostUtil;

import static model.PostUtil.PAGE_LENGTH;

@Path("/")
@Controller
public class HomeController {
	@Inject
	Models models;
	
	@Inject
	PostRepository posts;

	@Inject
	Database database;
	
	@GET
	public String get(@QueryParam("start") String startParam) throws JsonException {
		int start;
		if(StringUtil.isNotEmpty(startParam)) {
			try {
				start = Integer.parseInt(startParam);
			} catch(NumberFormatException e) {
				start = -1;
			}
		} else {
			start = -1;
		}
		if(start > -1) {
			models.put("posts", posts.homeList(start, PAGE_LENGTH));
			models.put("start", start);
			models.put("pageSize", PAGE_LENGTH);

			int total = start + PAGE_LENGTH;
			if(total >= PostUtil.getPostCount()) {
				models.put("endOfLine", true);
			} else {
				models.put("endOfLine", false);
			}
		} else {
			models.put("posts", posts.homeList()); //$NON-NLS-1$
		}
		
		return "home.jsp"; //$NON-NLS-1$
	}
}