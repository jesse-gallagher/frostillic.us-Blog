package security.jaxrs;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.darwino.platform.DarwinoContext;

import security.DarwinoPrincipal;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class DarwinoJAXRSSecurityContext implements SecurityContext {
	private final UriInfo uriInfo;
	
	public DarwinoJAXRSSecurityContext(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	@Override
	public Principal getUserPrincipal() {
		return new DarwinoPrincipal(DarwinoContext.get().getUser());
	}

	@Override
	public boolean isUserInRole(String role) {
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