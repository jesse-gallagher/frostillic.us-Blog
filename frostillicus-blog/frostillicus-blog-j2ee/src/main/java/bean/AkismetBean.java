/*
 * Copyright © 2012-2023 Jesse Gallagher
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
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import api.akismet.Akismet11Client;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.HttpHeaders;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.darwino.commons.util.StringUtil;

import darwino.AppDatabaseDef;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import util.HttpUtil;

@Named("akismet")
@RequestScoped
public class AkismetBean {
	public static final String USER_AGENT = "frostillic.us/2.0 Akismet/2.0"; //$NON-NLS-1$
	public static final String REQUEST_PROTOCOL = "https"; //$NON-NLS-1$
	public static final String BASE_HOST = "rest.akismet.com"; //$NON-NLS-1$
	public static final String BASE_PATH = "rest.akismet.com/1.1"; //$NON-NLS-1$
	public static final String VERIFY_KEY_URL = "rest.akismet.com/1.1/verify-key"; //$NON-NLS-1$
	public static final String COMMENT_CHECK_URL = "rest.akismet.com/1.1/comment-check"; //$NON-NLS-1$
	public static final String TYPE_COMMENT = "comment"; //$NON-NLS-1$

	@Inject
	@ConfigProperty(name=AppDatabaseDef.DATABASE_NAME+".akismet-api-key", defaultValue="")
	@Getter @Setter
	private String apiKey;
	@Inject
	@ConfigProperty(name=AppDatabaseDef.DATABASE_NAME+".akismet-blog", defaultValue="")
	@Getter @Setter
	private String blog;

	public boolean isValid() {
		return StringUtil.isNotEmpty(this.apiKey) && StringUtil.isNotEmpty(this.blog);
	}

	public boolean verifyKey() throws Exception {
		if(StringUtil.isEmpty(this.apiKey)) { throw new IllegalArgumentException("apiKey is empty"); } //$NON-NLS-1$
		if(StringUtil.isEmpty(this.blog)) { throw new IllegalArgumentException("blog is key"); } //$NON-NLS-1$

		Map<String, String> params = new HashMap<>();
		params.put("key", this.apiKey); //$NON-NLS-1$
		params.put("blog", this.blog); //$NON-NLS-1$

		String response = HttpUtil.doPost(REQUEST_PROTOCOL + "://" + VERIFY_KEY_URL, "akismet", Collections.singletonMap(HttpHeaders.USER_AGENT, USER_AGENT), params); //$NON-NLS-1$ //$NON-NLS-2$

		return response.equals("valid"); //$NON-NLS-1$
	}

	public boolean checkComment(final String remoteAddress, final String userAgent, final String referrer, final String permalink, final String commentType, final String author, final String authorEmail, final String authorURL, final String content) throws Exception {
		if(StringUtil.isEmpty(this.apiKey)) { throw new IllegalArgumentException("apiKey is empty"); } //$NON-NLS-1$
		if(StringUtil.isEmpty(this.blog)) { throw new IllegalArgumentException("blog is key"); } //$NON-NLS-1$

		KeyStore keystore = HttpUtil.loadKeyStore("akismet"); //$NON-NLS-1$
		Akismet11Client client = RestClientBuilder.newBuilder()
			.baseUri(new URI(REQUEST_PROTOCOL + "://" + this.apiKey + "." + Akismet11Client.BASE_HOST)) //$NON-NLS-1$ //$NON-NLS-2$
			.trustStore(keystore)
			.build(Akismet11Client.class);

		return client.checkComment(this.blog, remoteAddress, userAgent, referrer, permalink, commentType, author, authorEmail, authorURL, content);
	}
}
