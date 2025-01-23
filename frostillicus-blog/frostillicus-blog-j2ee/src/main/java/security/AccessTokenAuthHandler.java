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
package security;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.darwino.commons.httpclnt.HttpBase;
import com.darwino.commons.httpclnt.HttpClient;
import com.darwino.commons.httpclnt.HttpClient.Authenticator;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.util.StringUtil;
import com.darwino.j2ee.servlet.authentication.handler.AbstractAuthHandler;
import com.darwino.platform.DarwinoApplication;

import darwino.AppDatabaseDef;
import jakarta.security.enterprise.credential.Credential;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
			var session = DarwinoApplication.get().getLocalJsonDBServer().createSystemSession(null);
			try {
				var database = session.getDatabase(AppDatabaseDef.DATABASE_NAME);
				var store = database.getStore(AppDatabaseDef.STORE_TOKENS);
				var query = JsonObject.of("token", token); //$NON-NLS-1$
				var tokenDoc = store.openCursor()
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
	public boolean hasAuthenticationInfo(final HttpServletRequest httpRequest) throws ServletException {
		var credentials = httpRequest.getHeader(HttpBase.HEADER_AUTHORIZATION);
		return StringUtil.isNotEmpty(credentials) && credentials.startsWith("Bearer "); //$NON-NLS-1$
	}

	@Override
	public AccessTokenAuthenticator readAuthentication(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse)
			throws IOException, ServletException {
		if(!hasAuthenticationInfo(httpRequest)) {
			return null;
		}
		var tokenVal = httpRequest.getHeader(HttpBase.HEADER_AUTHORIZATION).substring("Bearer ".length()); //$NON-NLS-1$
		var auth = new AccessTokenAuthenticator(tokenVal);
		if(StringUtil.isNotEmpty(auth.getDn())) {
			return auth;
		} else {
			return null;
		}
	}

	@Override
	public String getUserLoginid(final Authenticator authenticator) {
		if(authenticator instanceof AccessTokenAuthenticator) {
			return ((AccessTokenAuthenticator)authenticator).getDn();
		}
		return null;
	}

	@Override
	public boolean authenticate(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final String redirectUrl)
			throws IOException, ServletException {
		return false;
	}

	@Override
	public void unauthenticate(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse)
			throws IOException, ServletException {
		// Can't
	}
}
