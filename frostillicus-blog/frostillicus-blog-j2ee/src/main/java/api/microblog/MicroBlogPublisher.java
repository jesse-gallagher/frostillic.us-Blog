/*
 * Copyright © 2012-2020 Jesse Gallagher
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.darwino.commons.util.StringUtil;

import api.micropub.MicroPubClient.EntryType;
import darwino.AppDatabaseDef;
import lombok.Getter;
import lombok.Setter;
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
	private static final Logger log = Logger.getLogger(MicroBlogPublisher.class.getPackage().getName());
	static {
		log.setLevel(Level.ALL);
	}

	private static final ExecutorService exec = Executors.newCachedThreadPool();

	@Inject
	@ConfigProperty(name=AppDatabaseDef.DATABASE_NAME+".microblog-key", defaultValue="")
	@Getter @Setter
	private String apiKey;

	public void crossPost(@Observes final MicroPostEvent event) {
		if(StringUtil.isNotEmpty(apiKey)) {
			if(log.isLoggable(Level.FINE)) {
				log.fine("Logging MicroPost " + event.getPost()); //$NON-NLS-1$
			}

			// Do this async since we don't want to fail or hold up the whole operation if there's a downstream issue.
			// TODO keep track of success so we can re-post down the line
			exec.submit(() -> {
				try {
					var post = event.getPost();

					// TODO switch to MicroProfile REST Client when it supports the keystore
					Map<String, String> auth = Collections.singletonMap(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey); //$NON-NLS-1$
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
