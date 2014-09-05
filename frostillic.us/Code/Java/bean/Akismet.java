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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Akismet {
	public static final String USER_AGENT = "frostillic.us/1.0 Akismet/1.0";
	public static final String REQUEST_PROTOCOL = "http";
	public static final String VERIFY_KEY_URL = "rest.akismet.com/1.1/verify-key";
	public static final String COMMENT_CHECK_URL = "rest.akismet.com/1.1/comment-check";

	private String apiKey;
	private String blog;


	// Blank constructor for bean use
	public Akismet() {}

	public Akismet(String apiKey, String blog) {
		this.apiKey = apiKey;
		this.blog = blog;
	}

	public void setApiKey(String apiKey) { this.apiKey = apiKey; }
	public String getApiKey() { return this.apiKey; }
	public void setBlog(String blog) { this.blog = blog; }
	public String getBlog() { return this.blog; }

	public boolean verifyKey() throws Exception {
		if(apiKey == null) { throw new IllegalArgumentException("apiKey is null"); }
		if(blog == null) { throw new IllegalArgumentException("blog is null"); }

		Map<String, String> params = new HashMap<String, String>();
		params.put("key", this.apiKey);
		params.put("blog", this.blog);

		String response = doPost(REQUEST_PROTOCOL + "://" + VERIFY_KEY_URL, params);

		return response.equals("valid");
	}

	public boolean checkComment(String remoteAddress, String userAgent, String referrer, String permalink, String commentType, String author, String authorEmail, String authorURL, String content) throws Exception {
		if(apiKey == null) { throw new IllegalArgumentException("apiKey is null"); }
		if(blog == null) { throw new IllegalArgumentException("blog is null"); }

		Map<String, String> params = new HashMap<String, String>();
		params.put("blog", this.blog);
		params.put("user_ip", remoteAddress);
		params.put("user_agent", userAgent);
		params.put("referrer", referrer);
		params.put("permalink", permalink);
		params.put("comment_type", commentType);
		params.put("comment_author", author);
		params.put("comment_author_email", authorEmail);
		params.put("comment_author_url", authorURL);
		params.put("comment_content", content);

		String response = doPost(REQUEST_PROTOCOL + "://" + this.apiKey + "." + COMMENT_CHECK_URL, params);

		return response.equals("true");
	}

	private String doPost(String url, Map<String, String> content) throws Exception {
		// Create a new POST request
		URL urlObj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
			requestContent.append(URLEncoder.encode(key, "UTF-8"));
			requestContent.append('=');
			requestContent.append(URLEncoder.encode(content.get(key), "UTF-8"));
		}

		// Generate the content and write it into the request
		String requestString = requestContent.toString();
		conn.setRequestProperty("Content-Length", Integer.toString(requestString.getBytes().length));
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