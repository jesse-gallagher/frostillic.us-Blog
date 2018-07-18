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
		if("POST".equals(requestContext.getMethod()) && requestContext.hasEntity()) {
			// TODO look for a non-implementation-specific way to do this
			if(requestContext.getRequest() instanceof RequestImpl) {
				// Check for a _method form param
				RequestImpl req = (RequestImpl)requestContext.getRequest();
				List<String> formVal = req.getFormParameters().get("_method");
				if(formVal != null && !formVal.isEmpty()) {
					requestContext.setMethod(formVal.get(0));
				}
			}
		}
	}

}
