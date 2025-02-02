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
package jaxrs;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Some web containers, such as WebSphere Liberty, serve JAX-RS HTML responses as ISO-8859-1
 * regardless of what the JSP file indicates. This filter checks for HTML responses and
 * forces their content charset to UTF-8.
 */
@Provider
public class UTFResponseFilter implements ContainerResponseFilter {
    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
        var contentType = responseContext.getHeaderString("Content-Type"); //$NON-NLS-1$
        if(contentType != null && contentType.startsWith("text/html")) { //$NON-NLS-1$
            // Set it to UTF-8
            responseContext.getHeaders().putSingle("Content-Type", "text/html;charset=UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
