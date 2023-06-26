/*
 * Copyright (c) 2012-2023 Jesse Gallagher
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

import com.darwino.commons.util.PathUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

@ApplicationScoped
@Named("urlBean")
public class UrlBean {
	@Inject
	private HttpServletRequest req;

	public URI getRequestUri() throws URISyntaxException {
		return new URI(req.getRequestURL().toString()).resolve(req.getContextPath() + "/"); //$NON-NLS-1$
	}

	public String relativizeUrl(final String url) {
		// In practice, the distinction in these URLs is whether or not they start with "/"
		if(url == null || url.isEmpty()) {
			return url;
		}

		if(url.charAt(0) == '/') {
			return PathUtil.concat(req.getContextPath(), url, '/');
		} else {
			return url;
		}
	}

	public String concat(final String... parts) {
		if(parts == null || parts.length == 0) {
			return ""; //$NON-NLS-1$
		}
		String result = parts[0];
		for(int i = 1; i < parts.length; i++) {
			if(!result.endsWith("/")) { //$NON-NLS-1$
				result += "/"; //$NON-NLS-1$
			}
			String part = parts[i];
			if(part.startsWith("/")) { //$NON-NLS-1$
				part = part.substring(1);
			}
			result += part;
		}
		return result;
	}
}
