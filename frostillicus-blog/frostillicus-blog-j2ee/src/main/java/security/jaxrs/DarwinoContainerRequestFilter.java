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
package security.jaxrs;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

/**
 *
 * @author Jesse Gallagher
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class DarwinoContainerRequestFilter implements ContainerRequestFilter {
	@Context
	UriInfo uriInfo;

	@Override
	public void filter(final ContainerRequestContext requestContext) throws IOException {
		requestContext.setSecurityContext(new DarwinoJAXRSSecurityContext(uriInfo));
	}

}