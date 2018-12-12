package jaxrs;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Some web containers, such as WebSphere Liberty, serve JAX-RS HTML responses as ISO-8859-1
 * regardless of what the JSP file indicates. This filter checks for HTML responses and
 * forces their content charset to UTF-8.
 */
@Provider
public class UTFFilterResponse implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String contentType = responseContext.getHeaderString("Content-Type");
        if(contentType != null && contentType.startsWith("text/html")) {
            // Set it to UTF-8
            responseContext.getHeaders().putSingle("Content-Type", "text/html;charset=UTF-8");
        }
    }
}
