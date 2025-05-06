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
package api.microblog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commons.util.StringUtil;

import api.micropub.MicroPubClient.EntryType;
import bean.ConfigBean;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.HttpHeaders;
import model.MicroPost;
import model.event.MicroPostEvent;
import util.HttpUtil;

/**
 * Publishes {@link MicroPost} entities to micro.blog, if configured.
 *
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@ApplicationScoped
public class MicroBlogPublisher {
	
	@Inject
	private Logger log;
	
	@Inject @Named("java:comp/DefaultManagedExecutorService")
	private ManagedExecutorService exec;

	@Inject
	private ConfigBean configBean;
	
	public String getApiKey() {
		return configBean.getConfig("microblog-key")
			.orElse(null);
	}

	public void crossPost(@Observes final MicroPostEvent event) {
		if(StringUtil.isNotEmpty(getApiKey())) {
			if(log.isLoggable(Level.FINE)) {
				log.fine("Logging MicroPost " + event.post()); //$NON-NLS-1$
			}

			// Do this async since we don't want to fail or hold up the whole operation if there's a downstream issue.
			// TODO keep track of success so we can re-post down the line
			exec.submit(() -> {
				try {
					var post = event.post();

					// TODO switch to MicroProfile REST Client when it supports the keystore
					Map<String, String> auth = Collections.singletonMap(HttpHeaders.AUTHORIZATION, "Bearer " + getApiKey()); //$NON-NLS-1$
					Map<String, String> content = new HashMap<>();
					content.put("h", EntryType.entry.name()); //$NON-NLS-1$
					content.put("name", post.getName()); //$NON-NLS-1$
					content.put("content", post.getContent()); //$NON-NLS-1$
					HttpUtil.doPost("https://micro.blog/micropub", "microblog", auth, content); //$NON-NLS-1$ //$NON-NLS-2$
				} catch(Throwable t) {
					t.printStackTrace();
				}
			});
		} else {
			if(log.isLoggable(Level.FINE)) {
				log.fine("micro.blog key or username is unset; skipping publishing"); //$NON-NLS-1$
			}
		}
	}
}
