package security;

import java.io.IOException;

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

import com.darwino.commons.httpclnt.HttpBase;
import com.darwino.commons.httpclnt.HttpClient;
import com.darwino.commons.util.StringUtil;
import com.darwino.commons.util.io.Base64Util;
import com.darwino.j2ee.servlet.authentication.handler.FormAuthHandler;

import security.AccessTokenAuthHandler.AccessTokenAuthenticator;

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
	public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {
		{
			AccessTokenAuthHandler handler = new AccessTokenAuthHandler();
			try {
				AccessTokenAuthenticator authenticator = handler.readAuthentication(request, response);
				if(authenticator != null) {
					CredentialValidationResult result = identityStore.validate(authenticator);
					if(result != null) {
						return httpMessageContext.notifyContainerAboutLogin(result.getCallerPrincipal(), result.getCallerGroups());
					}
				}
			} catch (IOException | ServletException e) {
				throw new AuthenticationException(e);
			}
		}
		
		// Check for Basic auth
        String authHeaderB64 = request.getHeader(HttpBase.HEADER_AUTHORIZATION);
        if(StringUtil.isNotEmpty(authHeaderB64) && authHeaderB64.startsWith("Basic ")) { //$NON-NLS-1$
            String authHeader = new String(Base64Util.decodeBase64(authHeaderB64.substring(authHeaderB64.indexOf(' ') + 1)));
            int i = authHeader.indexOf(':');
            if (i > 0) {
                String userName = authHeader.substring(0, i);
                String password = i == authHeader.length()-1 ? "" : authHeader.substring(i+1); //$NON-NLS-1$
                CredentialValidationResult result = identityStore.validate(new UsernamePasswordCredential(userName, password));
                return httpMessageContext.notifyContainerAboutLogin(result.getCallerPrincipal(), result.getCallerGroups());
            } else {
                return httpMessageContext.responseUnauthorized();
            }
        }

        // Failing that, check for form auth
        {
	        FormAuthHandler handler = new FormAuthHandler();
	        try {
	            HttpClient.Authenticator auth = handler.readAuthentication(request, response);
	            if(auth instanceof HttpClient.BasicAuthenticator) {
	                HttpClient.BasicAuthenticator cred = (HttpClient.BasicAuthenticator)auth;
	                CredentialValidationResult result = identityStore.validate(new UsernamePasswordCredential(cred.getUserName(), cred.getPassword()));
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
