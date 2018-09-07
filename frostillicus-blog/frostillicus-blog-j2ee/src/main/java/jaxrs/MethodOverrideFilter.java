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
package jaxrs;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.specimpl.RequestImpl;

@Provider
@PreMatching
public class MethodOverrideFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if("POST".equals(requestContext.getMethod()) && requestContext.hasEntity()) { //$NON-NLS-1$
			// TODO look for a non-implementation-specific way to do this
			if(requestContext.getRequest() instanceof RequestImpl) {
				// Check for a _method form param
				RequestImpl req = (RequestImpl)requestContext.getRequest();
				List<String> formVal = req.getFormParameters().get("_method"); //$NON-NLS-1$
				if(formVal != null && !formVal.isEmpty()) {
					requestContext.setMethod(formVal.get(0));
				}
			}
		}
	}

}
