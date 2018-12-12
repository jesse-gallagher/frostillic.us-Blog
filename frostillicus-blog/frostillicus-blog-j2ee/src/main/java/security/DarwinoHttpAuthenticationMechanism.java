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

import com.darwino.commons.httpclnt.HttpClient;
import com.darwino.j2ee.servlet.authentication.handler.FormAuthHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ApplicationScoped
public class DarwinoHttpAuthenticationMechanism implements HttpAuthenticationMechanism {

    @Inject
    IdentityStore identityStore;
    
    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {
        FormAuthHandler handler = new FormAuthHandler();
        try {
            HttpClient.Authenticator auth = handler.readAuthentication(request, response);
            if(auth instanceof HttpClient.BasicAuthenticator) {
                HttpClient.BasicAuthenticator cred = (HttpClient.BasicAuthenticator)auth;
                CredentialValidationResult result = identityStore.validate(new UsernamePasswordCredential(cred.getUserName(), cred.getPassword()));
                httpMessageContext.notifyContainerAboutLogin(result.getCallerPrincipal(), result.getCallerGroups());
            }
        } catch (IOException | ServletException e) {
            throw new AuthenticationException(e);
        }
        return httpMessageContext.doNothing();
    }
}
