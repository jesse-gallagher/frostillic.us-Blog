package bean;

import java.net.URI;
import java.net.URISyntaxException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@RequestScoped
@Named("uriInfoBean")
public class UriInfoBean {
    @Inject
	private HttpServletRequest request;
	
	public URI getRequestUri() throws URISyntaxException {
		return new URI(request.getRequestURL().toString()).resolve(request.getContextPath() + "/"); //$NON-NLS-1$
	}
}
