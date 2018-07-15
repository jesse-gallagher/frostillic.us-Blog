/**
 * Copyright © 2016-2018 Jesse Gallagher
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
import org.darwino.jnosql.diana.driver.DarwinoDocumentCollectionManagerFactory;
import org.darwino.jnosql.diana.driver.DarwinoDocumentConfiguration;
import org.jnosql.diana.api.document.DocumentCollectionManagerFactory;
import org.jnosql.diana.api.document.DocumentConfiguration;

@ApplicationScoped
public class DocumentCollectionManagerProducer {

	private DocumentConfiguration<DarwinoDocumentCollectionManagerFactory> configuration;
	private DocumentCollectionManagerFactory<DarwinoDocumentCollectionManager> managerFactory;
	
	@PostConstruct
	public void init() {
		configuration = new DarwinoDocumentConfiguration();
		managerFactory = configuration.get();
	}
	
	@Produces
	public DarwinoDocumentCollectionManager getManager() {
		return managerFactory.get(com.darwino.jsonstore.Database.STORE_DEFAULT);
	}
}
