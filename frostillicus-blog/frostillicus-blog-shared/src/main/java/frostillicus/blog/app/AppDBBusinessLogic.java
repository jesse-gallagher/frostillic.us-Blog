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
package frostillicus.blog.app;

import com.darwino.jsonstore.extensions.DefaultExtensionRegistry;

/**
 * Database Business logic - event handlers.
 */
public  class AppDBBusinessLogic extends DefaultExtensionRegistry {
	
	public AppDBBusinessLogic() {
		// Add here the database events to register to the JSON store
//		registerDocumentEvents("<My Database Id>", "<My Store Id>", new DocumentEvents() {
//			@Override
//			public void querySaveDocument(Document doc) throws JsonException {
//			}
//		});
		
		
	}
}
