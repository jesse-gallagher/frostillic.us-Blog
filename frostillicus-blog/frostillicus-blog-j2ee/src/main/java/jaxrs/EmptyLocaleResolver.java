/*
 * Copyright (c) 2012-2025 Jesse Gallagher
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
package jaxrs;

import java.util.Locale;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mvc.locale.LocaleResolver;
import jakarta.mvc.locale.LocaleResolverContext;
import jakarta.ws.rs.core.HttpHeaders;

/**
 * Accounts for cases where a client sends an empty Accept-Language header,
 * which causes an "invalid locale" exception on CXF.
 *
 * @author Jesse Gallagher
 * @since 2.3.0
 * @see <a href="https://github.com/jesse-gallagher/frostillic.us-Blog/issues/79">Issue #79</a>
 */
@ApplicationScoped
@Priority(1) // Must be before org.eclipse.krazo.locale.DefaultLocaleResolver
public class EmptyLocaleResolver implements LocaleResolver {

	@Override
	public Locale resolveLocale(final LocaleResolverContext context) {
		var langHeader = context.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE);
		if("".equals(langHeader)) { //$NON-NLS-1$
			// Then it's an empty but extant header
			return Locale.getDefault();
		}
		return null;
	}

}
