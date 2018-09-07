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
package security;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.darwino.platform.DarwinoContext;

/**
 * Adapter for the Java EE 8 Security API 1.0 to work with a Darwino backend.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class DarwinoSecurityContext implements SecurityContext {

	@Override
	public Principal getCallerPrincipal() {
		return new DarwinoPrincipal(DarwinoContext.get().getUser());
	}

	@Override
	public <T extends Principal> Set<T> getPrincipalsByType(Class<T> pType) {
		// Not sure if I should do anything with this
		return Collections.emptySet();
	}

	@Override
	public boolean isCallerInRole(String role) {
		return DarwinoContext.get().getUser().hasRole(role);
	}

	@Override
	public boolean hasAccessToWebResource(String resource, String... methods) {
		// TODO Figure out if there's a good implementation here
		return true;
	}

	@Override
	public AuthenticationStatus authenticate(HttpServletRequest request, HttpServletResponse response, AuthenticationParameters parameters) {
		
		return null;
	}

}
