/*
 * ? Copyright Jesse Gallagher 2012
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package bean;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import frostillicus.xsp.bean.ManagedBean;
import frostillicus.xsp.bean.RequestScoped;
import frostillicus.xsp.util.FrameworkUtils;

@ManagedBean(name="akismet")
@RequestScoped
public class Akismet {
	public static final String USER_AGENT = "frostillic.us/1.0 Akismet/1.0"; //$NON-NLS-1$
	public static final String REQUEST_PROTOCOL = "http"; //$NON-NLS-1$
	public static final String VERIFY_KEY_URL = "rest.akismet.com/1.1/verify-key"; //$NON-NLS-1$
	public static final String COMMENT_CHECK_URL = "rest.akismet.com/1.1/comment-check"; //$NON-NLS-1$

	private String apiKey;
	private String blog;

	public static Akismet get() {
		Akismet existing = (Akismet)FrameworkUtils.resolveVariable(Akismet.class.getAnnotation(ManagedBean.class).name());
		return existing == null ? new Akismet() : existing;
	}

	// Blank constructor for bean use
	public Akismet() {}

	public Akismet(final String apiKey, final String blog) {
		this.apiKey = apiKey;
		this.blog = blog;
	}

	public void setApiKey(final String apiKey) { this.apiKey = apiKey; }
	public String getApiKey() { return this.apiKey; }
	public void setBlog(final String blog) { this.blog = blog; }
	public String getBlog() { return this.blog; }

	public boolean verifyKey() throws Exception {
		if(apiKey == null) { throw new IllegalArgumentException("apiKey is null"); } //$NON-NLS-1$
		if(blog == null) { throw new IllegalArgumentException("blog is null"); } //$NON-NLS-1$

		Map<String, String> params = new HashMap<String, String>();
		params.put("key", this.apiKey); //$NON-NLS-1$
		params.put("blog", this.blog); //$NON-NLS-1$

		String response = doPost(REQUEST_PROTOCOL + "://" + VERIFY_KEY_URL, params); //$NON-NLS-1$

		return response.equals("valid"); //$NON-NLS-1$
	}

	public boolean checkComment(final String remoteAddress, final String userAgent, final String referrer, final String permalink, final String commentType, final String author, final String authorEmail, final String authorURL, final String content) throws IOException {
		if(apiKey == null) { throw new IllegalArgumentException("apiKey is null"); } //$NON-NLS-1$
		if(blog == null) { throw new IllegalArgumentException("blog is null"); } //$NON-NLS-1$

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

	private String doPost(final String url, final Map<String, String> content) throws IOException {
		// Create a new POST request
		URL urlObj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();
		conn.setRequestMethod("POST"); //$NON-NLS-1$
		conn.setRequestProperty("User-Agent", USER_AGENT); //$NON-NLS-1$
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); //$NON-NLS-1$ //$NON-NLS-2$
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);

		// Generate the content from the parameter map
		StringBuilder requestContent = new StringBuilder();
		for(String key : content.keySet()) {
			if(requestContent.length() > 0) {
				requestContent.append('&');
			}
			//System.out.println("Encoding " + key + " => " + content.get(key));
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
}