/*
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
package api.rsd;

import java.util.ResourceBundle;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.reflections.Reflections;
import org.w3c.dom.Document;

import api.atompub.AtomPubResource;

@Path("/rsd.xml")
public class ReallySimpleDiscoveryResource {

	private static final Set<Class<?>> serviceClasses = new Reflections("api").getTypesAnnotatedWith(RSDService.class); //$NON-NLS-1$

	@Inject @Named("translation")
	ResourceBundle translation;

	@Context
	UriInfo uriInfo;

	@GET
	@Produces("application/rsd+xml")
	public Document get() {
	       return null;
	}
}
