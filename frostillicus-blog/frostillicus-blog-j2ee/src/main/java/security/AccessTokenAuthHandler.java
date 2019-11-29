package security;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.security.enterprise.credential.Credential;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.darwino.commons.httpclnt.HttpBase;
import com.darwino.commons.httpclnt.HttpClient;
import com.darwino.commons.httpclnt.HttpClient.Authenticator;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.util.StringUtil;
import com.darwino.j2ee.servlet.authentication.handler.AbstractAuthHandler;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Document;
import com.darwino.jsonstore.Session;
import com.darwino.jsonstore.Store;
import com.darwino.platform.DarwinoApplication;

import darwino.AppDatabaseDef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * Authentication handler for the app's ad-hoc not-really-OAuth "Bearer" tokens.
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public class AccessTokenAuthHandler extends AbstractAuthHandler {
	
	@AllArgsConstructor
	public static class AccessTokenAuthenticator extends HttpClient.Authenticator implements Credential {
		private static final long serialVersionUID = 1L;
		
		private final @Getter String token;
		
		@SneakyThrows
		public String getDn() {
			Session session = DarwinoApplication.get().getLocalJsonDBServer().createSystemSession(null);
			try {
				Database database = session.getDatabase(AppDatabaseDef.DATABASE_NAME);
				Store store = database.getStore(AppDatabaseDef.STORE_TOKENS);
				JsonObject query = JsonObject.of("token", token); //$NON-NLS-1$
				Document tokenDoc = store.openCursor()
					.query(query)
					.findOneDocument();
				return tokenDoc == null ? null : tokenDoc.getString("userName"); //$NON-NLS-1$
			} finally {
				session.close();
			}
		}
		
		@Override
		public boolean isValid() {
			return StringUtil.isNotEmpty(getDn());
		}
		
		@Override
		public Map<String, String> getAuthenticationHeaders() {
			if(isValid()) {
				return Collections.singletonMap(HttpBase.HEADER_AUTHORIZATION, "Bearer " + token); //$NON-NLS-1$
			}
			return null;
		}
	}

	@Override
	public boolean hasAuthenticationInfo(HttpServletRequest httpRequest) throws ServletException {
		String credentials = httpRequest.getHeader(HttpBase.HEADER_AUTHORIZATION);
		return StringUtil.isNotEmpty(credentials) && credentials.startsWith("Bearer "); //$NON-NLS-1$
	}

	@Override
	public AccessTokenAuthenticator readAuthentication(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException, ServletException {
		if(!hasAuthenticationInfo(httpRequest)) {
			return null;
		}
		String tokenVal = httpRequest.getHeader(HttpBase.HEADER_AUTHORIZATION).substring("Bearer ".length()); //$NON-NLS-1$
		AccessTokenAuthenticator auth = new AccessTokenAuthenticator(tokenVal);
		if(StringUtil.isNotEmpty(auth.getDn())) {
			return auth;
		} else {
			return null;
		}
	}

	@Override
	public String getUserLoginid(Authenticator authenticator) {
		if(authenticator instanceof AccessTokenAuthenticator) {
			return ((AccessTokenAuthenticator)authenticator).getDn();
		}
		return null;
	}

	@Override
	public boolean authenticate(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String redirectUrl)
			throws IOException, ServletException {
		return false;
	}

	@Override
	public void unauthenticate(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException, ServletException {
		// Can't
	}
}
