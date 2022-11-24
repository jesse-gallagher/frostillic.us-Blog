/*
 * Copyright © 2012-2022 Jesse Gallagher
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

import java.security.Principal;

import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import com.darwino.platform.DarwinoContext;

import security.DarwinoPrincipal;

/**
 *
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class DarwinoJAXRSSecurityContext implements SecurityContext {
	private final UriInfo uriInfo;

	public DarwinoJAXRSSecurityContext(final UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	@Override
	public Principal getUserPrincipal() {
		return new DarwinoPrincipal(DarwinoContext.get().getUser());
	}

	@Override
	public boolean isUserInRole(final String role) {
		return DarwinoContext.get().getUser().hasRole(role);
	}

	@Override
	public boolean isSecure() {
		return uriInfo.getAbsolutePath().toString().startsWith("https"); //$NON-NLS-1$
	}

	@Override
	public String getAuthenticationScheme() {
		// TODO look this up from the active authentication filter
		return FORM_AUTH;
	}

}