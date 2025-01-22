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
package bean;

import java.net.URI;
import java.security.KeyStore;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import com.darwino.commons.util.StringUtil;

import api.akismet.Akismet11Client;
import darwino.AppDatabaseDef;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import util.HttpUtil;

@Named("akismet")
@ApplicationScoped
public class AkismetBean {
	public static final String REQUEST_PROTOCOL = "https"; //$NON-NLS-1$
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

		KeyStore keystore = HttpUtil.loadKeyStore("akismet"); //$NON-NLS-1$
		try(Akismet11Client client = buildClient(keystore)) {
			return "valid".equals(client.verifyKey(this.apiKey, this.blog)); //$NON-NLS-1$
		}
	}

	public boolean checkComment(final String remoteAddress, final String userAgent, final String referrer, final String permalink, final String commentType, final String author, final String authorEmail, final String authorURL, final String content) throws Exception {
		if(StringUtil.isEmpty(this.apiKey)) { throw new IllegalArgumentException("apiKey is empty"); } //$NON-NLS-1$
		if(StringUtil.isEmpty(this.blog)) { throw new IllegalArgumentException("blog is key"); } //$NON-NLS-1$

		KeyStore keystore = HttpUtil.loadKeyStore("akismet"); //$NON-NLS-1$
		try(Akismet11Client client = buildClient(keystore)) {
			return client.checkComment(this.blog, remoteAddress, userAgent, referrer, permalink, commentType, author, authorEmail, authorURL, content);
		}
	}
	
	private Akismet11Client buildClient(KeyStore keystore) {
		return RestClientBuilder.newBuilder()
			.baseUri(URI.create(REQUEST_PROTOCOL + "://" + this.apiKey + "." + Akismet11Client.BASE_HOST)) //$NON-NLS-1$ //$NON-NLS-2$
			.trustStore(keystore)
			.build(Akismet11Client.class);
	}
}
