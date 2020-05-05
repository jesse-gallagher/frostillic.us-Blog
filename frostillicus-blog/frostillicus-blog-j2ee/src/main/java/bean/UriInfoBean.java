/**
 * Copyright © 2012-2020 Jesse Gallagher
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
package bean;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

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
	
	/**
	 * Retrieves the given request query parameter with UTF-8 encoding.
	 * 
	 * <p>This is needed because the JSP-available {@code param} object uses ISO-8859-1 for incoming
	 * query string parameters and breaks on non-ASCII values.</p>
	 * 
	 * @param param the name of the query parameter
	 * @return the value of the parameter as a UTF-8 string, or {@code null} if the parameter
	 * 		is not in the URL
	 * @see <a href="https://forums.adobe.com/thread/2337637">https://forums.adobe.com/thread/2337637</a>
	 * @since 2.2.0
	 */
	public String getParam(String param) {
		String val = request.getParameter(param);
		if(val == null) {
			return null;
		}
		return new String(val.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
	}
}
