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
package security;

import java.io.IOException;

import com.darwino.commons.httpclnt.HttpBase;
import com.darwino.commons.httpclnt.HttpClient;
import com.darwino.commons.util.StringUtil;
import com.darwino.commons.util.io.Base64Util;
import com.darwino.j2ee.servlet.authentication.handler.FormAuthHandler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Jakarta EE HTTP authentication mechanism adapter for {@link AccessTokenAuthHandler}.
 *
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@ApplicationScoped
public class AccessTokenAuthenticationMechanism implements HttpAuthenticationMechanism {
    @Inject
    IdentityStore identityStore;

	@Override
	public AuthenticationStatus validateRequest(final HttpServletRequest request, final HttpServletResponse response, final HttpMessageContext httpMessageContext) throws AuthenticationException {
		{
			var handler = new AccessTokenAuthHandler();
			try {
				var authenticator = handler.readAuthentication(request, response);
				if(authenticator != null) {
					var result = identityStore.validate(authenticator);
					if(result != null) {
						return httpMessageContext.notifyContainerAboutLogin(result.getCallerPrincipal(), result.getCallerGroups());
					}
				}
			} catch (IOException | ServletException e) {
				throw new AuthenticationException(e);
			}
		}

		// Check for Basic auth
        var authHeaderB64 = request.getHeader(HttpBase.HEADER_AUTHORIZATION);
        if(StringUtil.isNotEmpty(authHeaderB64) && authHeaderB64.startsWith("Basic ")) { //$NON-NLS-1$
            var authHeader = new String(Base64Util.decodeBase64(authHeaderB64.substring(authHeaderB64.indexOf(' ') + 1)));
            var i = authHeader.indexOf(':');
            if (i > 0) {
                var userName = authHeader.substring(0, i);
                var password = i == authHeader.length()-1 ? "" : authHeader.substring(i+1); //$NON-NLS-1$
                var result = identityStore.validate(new UsernamePasswordCredential(userName, password));
                return httpMessageContext.notifyContainerAboutLogin(result.getCallerPrincipal(), result.getCallerGroups());
            } else {
                return httpMessageContext.responseUnauthorized();
            }
        }

        // Failing that, check for form auth
        {
	        var handler = new FormAuthHandler();
	        try {
	            var auth = handler.readAuthentication(request, response);
	            if(auth instanceof HttpClient.BasicAuthenticator) {
	                var cred = (HttpClient.BasicAuthenticator)auth;
	                var result = identityStore.validate(new UsernamePasswordCredential(cred.getUserName(), cred.getPassword()));
	                return httpMessageContext.notifyContainerAboutLogin(result.getCallerPrincipal(), result.getCallerGroups());
	            }
	        } catch (IOException | ServletException e) {
	            e.printStackTrace();
	            throw new AuthenticationException(e);
	        }
        }

		return httpMessageContext.doNothing();
	}

}
