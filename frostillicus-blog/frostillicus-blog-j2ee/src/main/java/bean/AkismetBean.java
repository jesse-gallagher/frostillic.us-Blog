package bean;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.darwino.commons.util.StringUtil;

import darwino.AppDatabaseDef;
import lombok.Getter;
import lombok.Setter;

@Named("akismet")
@RequestScoped
public class AkismetBean {
	public static final String USER_AGENT = "frostillic.us/2.0 Akismet/2.0"; //$NON-NLS-1$
	public static final String REQUEST_PROTOCOL = "https"; //$NON-NLS-1$
	public static final String BASE_HOST = "rest.akismet.com"; //$NON-NLS-1$
	public static final String VERIFY_KEY_URL = "rest.akismet.com/1.1/verify-key"; //$NON-NLS-1$
	public static final String COMMENT_CHECK_URL = "rest.akismet.com/1.1/comment-check"; //$NON-NLS-1$
	public static final String TYPE_COMMENT = "comment"; //$NON-NLS-1$

	@Inject
	@ConfigProperty(name=AppDatabaseDef.DATABASE_NAME+".akismet-api-key")
	@Getter @Setter
	private String apiKey;
	@Inject
	@ConfigProperty(name=AppDatabaseDef.DATABASE_NAME+".akismet-blog")
	@Getter @Setter
	private String blog;

	public boolean verifyKey() throws Exception {
		if(StringUtil.isEmpty(this.apiKey)) { throw new IllegalArgumentException("apiKey is empty"); } //$NON-NLS-1$
		if(StringUtil.isEmpty(this.blog)) { throw new IllegalArgumentException("blog is key"); } //$NON-NLS-1$

		Map<String, String> params = new HashMap<String, String>();
		params.put("key", this.apiKey); //$NON-NLS-1$
		params.put("blog", this.blog); //$NON-NLS-1$

		String response = doPost(REQUEST_PROTOCOL + "://" + VERIFY_KEY_URL, params); //$NON-NLS-1$

		return response.equals("valid"); //$NON-NLS-1$
	}

	public boolean checkComment(final String remoteAddress, final String userAgent, final String referrer, final String permalink, final String commentType, final String author, final String authorEmail, final String authorURL, final String content) throws Exception {
		if(StringUtil.isEmpty(this.apiKey)) { throw new IllegalArgumentException("apiKey is empty"); } //$NON-NLS-1$
		if(StringUtil.isEmpty(this.blog)) { throw new IllegalArgumentException("blog is key"); } //$NON-NLS-1$

		Map<String, String> params = new HashMap<String, String>();
		params.put("blog", this.blog); //$NON-NLS-1$
		params.put("user_ip", remoteAddress); //$NON-NLS-1$
		params.put("user_agent", userAgent); //$NON-NLS-1$
		params.put("referrer", referrer); //$NON-NLS-1$
		params.put("permalink", permalink); //$NON-NLS-1$
		params.put("comment_type", commentType); //$NON-NLS-1$
		params.put("comment_author", author); //$NON-NLS-1$
		params.put("comment_author_email", authorEmail); //$NON-NLS-1$
		params.put("comment_author_url", authorURL); //$NON-NLS-1$
		params.put("comment_content", content); //$NON-NLS-1$

		String response = doPost(REQUEST_PROTOCOL + "://" + this.apiKey + "." + COMMENT_CHECK_URL, params); //$NON-NLS-1$ //$NON-NLS-2$

		return response.equals("true"); //$NON-NLS-1$
	}

	// TODO move to MicroProfile REST Client when the keystore options work in Liberty
	private String doPost(final String url, final Map<String, String> content) throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, KeyManagementException, UnrecoverableKeyException {
		// Create a new POST request
		URL urlObj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();
		conn.setRequestMethod("POST"); //$NON-NLS-1$
		conn.setRequestProperty("User-Agent", USER_AGENT); //$NON-NLS-1$
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); //$NON-NLS-1$ //$NON-NLS-2$
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		
		if(conn instanceof HttpsURLConnection) {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(loadKeyStore());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(loadKeyStore(), "akismet".toCharArray()); //$NON-NLS-1$
			SSLContext sslCtx = SSLContext.getInstance("TLS"); //$NON-NLS-1$
			sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			SSLSocketFactory sf = sslCtx.getSocketFactory();
			((HttpsURLConnection)conn).setSSLSocketFactory(sf);
		}

		// Generate the content from the parameter map
		StringBuilder requestContent = new StringBuilder();
		for(String key : content.keySet()) {
			if(requestContent.length() > 0) {
				requestContent.append('&');
			}
			requestContent.append(URLEncoder.encode(key, "UTF-8")); //$NON-NLS-1$
			requestContent.append('=');
			requestContent.append(URLEncoder.encode(content.get(key), "UTF-8")); //$NON-NLS-1$
		}

		// Generate the content and write it into the request
		String requestString = requestContent.toString();
		conn.setRequestProperty("Content-Length", Integer.toString(requestString.getBytes().length)); //$NON-NLS-1$
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(requestString);
		wr.flush();
		wr.close();

		InputStream is = conn.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder response = new StringBuilder();
		while(reader.ready()) {
			response.append((char)reader.read());
		}
		reader.close();

		return response.toString();
	}
	
	private KeyStore loadKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		KeyStore keystore = KeyStore.getInstance("JKS"); //$NON-NLS-1$
		try(InputStream is = getClass().getResourceAsStream("/akismet.jks")) { //$NON-NLS-1$
			keystore.load(is, "akismet".toCharArray()); //$NON-NLS-1$
		}
		return keystore;
	}
}
