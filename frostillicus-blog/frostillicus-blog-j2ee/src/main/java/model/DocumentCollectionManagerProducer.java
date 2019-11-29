/**
 * Copyright © 2012-2019 Jesse Gallagher
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
package model;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.darwino.jnosql.diana.driver.DarwinoDocumentCollectionManager;
import org.darwino.jnosql.diana.driver.DarwinoDocumentConfiguration;
import jakarta.nosql.mapping.Database;
import jakarta.nosql.mapping.DatabaseType;
import jakarta.nosql.document.DocumentCollectionManagerFactory;
import jakarta.nosql.document.DocumentConfiguration;

import darwino.AppDatabaseDef;

@ApplicationScoped
public class DocumentCollectionManagerProducer {

	private DocumentConfiguration configuration;
	private DocumentCollectionManagerFactory managerFactory;
	
	@PostConstruct
	public void init() {
		configuration = new DarwinoDocumentConfiguration();
		managerFactory = configuration.get();
	}
	
	@Produces
	public DarwinoDocumentCollectionManager getManager() {
		return managerFactory.get(com.darwino.jsonstore.Database.STORE_DEFAULT);
	}
	
	@Produces
	@Database(value=DatabaseType.DOCUMENT, provider=AppDatabaseDef.STORE_POSTS)
	public DarwinoDocumentCollectionManager getPostsManager() {
		return managerFactory.get(AppDatabaseDef.STORE_POSTS);
	}
	
	@Produces
	@Database(value=DatabaseType.DOCUMENT, provider=AppDatabaseDef.STORE_COMMENTS)
	public DarwinoDocumentCollectionManager getCommentsManager() {
		return managerFactory.get(AppDatabaseDef.STORE_COMMENTS);
	}
	
	@Produces
	@Database(value=DatabaseType.DOCUMENT, provider=AppDatabaseDef.STORE_CONFIG)
	public DarwinoDocumentCollectionManager getConfigManager() {
		return managerFactory.get(AppDatabaseDef.STORE_CONFIG);
	}
	
	@Produces
	@Database(value=DatabaseType.DOCUMENT, provider=AppDatabaseDef.STORE_MEDIA)
	public DarwinoDocumentCollectionManager getMediaManager() {
		return managerFactory.get(AppDatabaseDef.STORE_MEDIA);
	}
	
	/**
	 * @since 2.3.0
	 */
	@Produces
	@Database(value=DatabaseType.DOCUMENT, provider=AppDatabaseDef.STORE_MICROPOSTS)
	public DarwinoDocumentCollectionManager getMicroPostManager() {
		return managerFactory.get(AppDatabaseDef.STORE_MICROPOSTS);
	}
	
	/**
	 * @since 2.3.0
	 */
	@Produces
	@Database(value=DatabaseType.DOCUMENT, provider=AppDatabaseDef.STORE_TOKENS)
	public DarwinoDocumentCollectionManager getAccessTokenManager() {
		return managerFactory.get(AppDatabaseDef.STORE_TOKENS);
	}
	
	/**
	 * @since 2.3.0
	 */
	@Produces
	@Database(value=DatabaseType.DOCUMENT, provider=AppDatabaseDef.STORE_WEBMENTIONS)
	public DarwinoDocumentCollectionManager getWebmentionManager() {
		return managerFactory.get(AppDatabaseDef.STORE_WEBMENTIONS);
	}
}
