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
package api.atompub;

import com.darwino.commons.json.JsonException;

import api.atompub.model.AppCategories;
import api.atompub.model.AtomCategory;
import bean.UserInfoBean;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import model.util.PostUtil;

@Path(AtomPubResource.BASE_PATH + "/{blogId}/categories")
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
public class CategoriesResource {
    @GET
    @Produces("application/atomserv+xml")
    public AppCategories list() throws JsonException {
    	AppCategories categories = new AppCategories();
    	categories.setFixed(false);

        PostUtil.getCategories()
        	.map(AtomCategory::new)
        	.forEach(categories.getCategories()::add);

        return categories;
    }
}
