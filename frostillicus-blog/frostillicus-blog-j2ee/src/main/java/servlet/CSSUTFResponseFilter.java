package servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

/**
 * Processes outbound CSS file requests to add a UTF-8 charset to the
 * content type
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@WebFilter(urlPatterns="*.css")
public class CSSUTFResponseFilter implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		response.setContentType("text/css;charset=UTF-8"); //$NON-NLS-1$
		
		chain.doFilter(request, response);
	}
}
