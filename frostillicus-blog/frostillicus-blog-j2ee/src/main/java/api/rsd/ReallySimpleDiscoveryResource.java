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
package api.rsd;

import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import com.darwino.commons.util.PathUtil;
import com.darwino.commons.xml.DomUtil;

import api.atompub.AtomPubAPI;

@Path("/rsd.xml")
public class ReallySimpleDiscoveryResource {
	
	@Inject @Named("translation")
	ResourceBundle translation;
	
	@Context
	UriInfo uriInfo;
	
	@GET
	@Produces("application/rsd+xml")
	public Document get() {
		Document doc = DomUtil.createDocument();
		
		Element rsd = DomUtil.createRootElement(doc, "rsd"); //$NON-NLS-1$
		rsd.setAttribute("version", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
		rsd.setAttribute("xmlns", "http://archipelago.phrasewise.com/rsd"); //$NON-NLS-1$ //$NON-NLS-2$
		
		Element service = DomUtil.createElement(rsd, "service"); //$NON-NLS-1$
		DomUtil.createElement(service, "engineName", translation.getString("appTitle")); //$NON-NLS-1$ //$NON-NLS-2$
		DomUtil.createElement(service, "engineLink", translation.getString("baseUrl")); //$NON-NLS-1$ //$NON-NLS-2$
		DomUtil.createElement(service, "homePageLink", uriInfo.getBaseUri().toString()); //$NON-NLS-1$
		
		Element apis = DomUtil.createElement(service, "apis"); //$NON-NLS-1$
		{
			Element atompub = DomUtil.createElement(apis, "api"); //$NON-NLS-1$
			atompub.setAttribute("name", "Atom"); //$NON-NLS-1$ //$NON-NLS-2$
			atompub.setAttribute("preferred", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			atompub.setAttribute("apiLink", PathUtil.concat(uriInfo.getBaseUri().toString(), AtomPubAPI.BASE_PATH)); //$NON-NLS-1$
		}
		
		return doc;
	}
}
