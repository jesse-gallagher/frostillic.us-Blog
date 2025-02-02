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
package util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.darwino.commons.util.StringUtil;

/**
 * Contains shorthand HTTP utilities to use until I can figure out how to use
 * custom keystores with the MicroProfile REST Client API properly.
 *
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public enum HttpUtil {
	;

	/**
	 *
	 * @param url the URL to POST to
	 * @param keyStoreName the name of the keystore, which is used both for its ClassLoader path and its password
	 * @param headers a {@link Map} of headers to set
	 * @param content a {@link Map} of URL-encoded form params
	 * @return the body content
	 */
	public static String doPost(final String url, final String keyStoreName, final Map<String, String> headers, final Map<String, String> content) throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, KeyManagementException, UnrecoverableKeyException {
		// Create a new POST request
		var urlObj = new URL(url);
		var conn = (HttpURLConnection)urlObj.openConnection();
		conn.setRequestMethod("POST"); //$NON-NLS-1$
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); //$NON-NLS-1$ //$NON-NLS-2$
		for(Map.Entry<String, String> header : headers.entrySet()) {
			conn.setRequestProperty(header.getKey(), header.getValue());
		}
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);

		if(conn instanceof HttpsURLConnection) {
			var sslCtx = buildSslContext(keyStoreName);
			var sf = sslCtx.getSocketFactory();
			((HttpsURLConnection)conn).setSSLSocketFactory(sf);
		}

		// Generate the content from the parameter map
		var requestContent = new StringBuilder();
		for(String key : content.keySet()) {
			if(requestContent.length() > 0) {
				requestContent.append('&');
			}
			requestContent.append(URLEncoder.encode(key, "UTF-8")); //$NON-NLS-1$
			requestContent.append('=');
			if(StringUtil.isNotEmpty(content.get(key))) {
				requestContent.append(URLEncoder.encode(content.get(key), "UTF-8")); //$NON-NLS-1$
			}
		}

		// Generate the content and write it into the request
		var requestString = requestContent.toString();
		conn.setRequestProperty("Content-Length", Integer.toString(requestString.getBytes().length)); //$NON-NLS-1$
		try (var wr = new DataOutputStream(conn.getOutputStream())) {
			wr.writeBytes(requestString);
			wr.flush();
		}

		StringBuilder response = new StringBuilder();
		try (var is = conn.getInputStream()) {
			try (var reader = new BufferedReader(new InputStreamReader(is))) {
				while (reader.ready()) {
					response.append((char) reader.read());
				}
			}
		}

		return response.toString();
	}

	public static SSLContext buildSslContext(final String keyStoreName) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(loadKeyStore(keyStoreName));
		var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(loadKeyStore(keyStoreName), "akismet".toCharArray()); //$NON-NLS-1$
		var sslCtx = SSLContext.getInstance("TLS"); //$NON-NLS-1$
		sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		return sslCtx;
	}

	public static KeyStore loadKeyStore(final String name) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		var keystore = KeyStore.getInstance("JKS"); //$NON-NLS-1$
		try(var is = HttpUtil.class.getResourceAsStream("/" + name + ".jks")) { //$NON-NLS-1$ //$NON-NLS-2$
			keystore.load(is, name.toCharArray());
		}
		return keystore;
	}
}
