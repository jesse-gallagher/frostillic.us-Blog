package security.jaxrs;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

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
	public void filter(ContainerRequestContext requestContext) throws IOException {
		requestContext.setSecurityContext(new DarwinoJAXRSSecurityContext(uriInfo));
	}

}