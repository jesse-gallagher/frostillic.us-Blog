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
package api.atompub;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.xml.DomUtil;

import bean.UserInfoBean;
import model.util.PostUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path(AtomPubAPI.BASE_PATH + "/{blogId}/categories")
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
public class CategoriesResource {
    @GET
    @Produces("application/atomserv+xml")
    public String list() throws JsonException {
        Document xml = DomUtil.createDocument();
        Element service = DomUtil.createRootElement(xml, "app:categories"); //$NON-NLS-1$
        service.setAttribute("xmlns:app", "http://www.w3.org/2007/app"); //$NON-NLS-1$ //$NON-NLS-2$
        service.setAttribute("xmlns:atom", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$
        service.setAttribute("fixed", "no");

        PostUtil.getCategories()
            .forEach(tag -> {
                Element category = DomUtil.createElement(service, "atom:category");
                category.setAttribute("term", tag);
            }
        );

        return DomUtil.getXMLString(xml);
    }
}
