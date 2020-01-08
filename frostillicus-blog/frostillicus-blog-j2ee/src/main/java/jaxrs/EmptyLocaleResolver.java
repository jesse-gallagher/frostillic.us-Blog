package jaxrs;

import java.util.Locale;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.mvc.locale.LocaleResolver;
import javax.mvc.locale.LocaleResolverContext;
import javax.ws.rs.core.HttpHeaders;

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
	public Locale resolveLocale(LocaleResolverContext context) {
		String langHeader = context.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE);
		if("".equals(langHeader)) { //$NON-NLS-1$
			// Then it's an empty but extant header
			return Locale.getDefault();
		}
		return null;
	}

}
