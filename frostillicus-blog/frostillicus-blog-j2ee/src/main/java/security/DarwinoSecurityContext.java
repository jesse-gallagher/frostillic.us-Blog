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
