/*
 * Copyright (c) 2012-2025 Jesse Gallagher
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
import java.util.Set;
import java.util.stream.Collectors;

import org.w3c.dom.Document;

import com.darwino.commons.util.PathUtil;
import com.darwino.commons.xml.DomUtil;

import api.atompub.AtomPubResource;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

@Path("/rsd.xml")
@ApplicationScoped
public class ReallySimpleDiscoveryResource {
	@Inject @Named("translation")
	ResourceBundle translation;

	@Context
	UriInfo uriInfo;
	
	private Set<Class<?>> serviceClasses;
	
	@PostConstruct
	public void init() {
		serviceClasses = CDI.current().getBeanManager().getBeans(RSD.class)
			.stream()
			.map(bean -> bean.getBeanClass())
			.collect(Collectors.toSet());
	}

	@GET
	@Produces("application/rsd+xml")
	public Document get() {
		var doc = DomUtil.createDocument();

		var rsd = DomUtil.createRootElement(doc, "rsd"); //$NON-NLS-1$
		rsd.setAttribute("version", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
		rsd.setAttribute("xmlns", "http://archipelago.phrasewise.com/rsd"); //$NON-NLS-1$ //$NON-NLS-2$

		var service = DomUtil.createElement(rsd, "service"); //$NON-NLS-1$
		DomUtil.createElement(service, "engineName", translation.getString("appTitle")); //$NON-NLS-1$ //$NON-NLS-2$
		DomUtil.createElement(service, "engineLink", translation.getString("baseUrl")); //$NON-NLS-1$ //$NON-NLS-2$
		DomUtil.createElement(service, "homePageLink", uriInfo.getBaseUri().toString()); //$NON-NLS-1$

		var apis = DomUtil.createElement(service, "apis"); //$NON-NLS-1$
		serviceClasses.forEach(clazz -> {
			var def = clazz.getAnnotation(RSDService.class);

			var api = DomUtil.createElement(apis, "api"); //$NON-NLS-1$
			api.setAttribute("name", def.name()); //$NON-NLS-1$
			api.setAttribute("preferred", String.valueOf(def.preferred())); //$NON-NLS-1$
			api.setAttribute("apiLink", PathUtil.concat(uriInfo.getBaseUri().toString(), def.basePath())); //$NON-NLS-1$
			api.setAttribute("blogID", AtomPubResource.BLOG_ID); //$NON-NLS-1$
		});

		return doc;
	}
}
