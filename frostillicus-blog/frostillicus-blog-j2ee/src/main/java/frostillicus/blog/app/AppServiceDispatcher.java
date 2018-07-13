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

import com.darwino.commons.services.HttpServiceFactories;
import com.darwino.j2ee.application.DarwinoJ2EEServiceDispatcherFilter;

/**
 * J2EE application.
 * 
 * @author Philippe Riand
 */
public class AppServiceDispatcher extends DarwinoJ2EEServiceDispatcherFilter {
	
	public AppServiceDispatcher() {
	}
	
// Here is some code sample for services customization
// You can uncomment what is need by your application
//	
//	/**
//	 * Init and customize the application services. 
//	 */
//	@Override
//	protected void initServicesFactories(FilterConfig config, HttpServiceFactories factories) {
//		super.initServicesFactories(config, factories);
//		
//		// Here is how to install a service manager, to control:
//		//   - Access the services (security), for example based on the caller
//		//	 - Manage stats about the services - count the successful calls to an API and manage quotas
//		HttpServiceManager serviceManager = new HttpServiceManager() {
//			@Override
//			public void preExecute(HttpServiceContext context, HttpServiceFactory factory, HttpService service) {
//			}
//			@Override
//			public void postExecute(HttpServiceContext context, HttpServiceFactory factory, HttpService service) {
//			}
//		};
//		factories.setServiceManager(serviceManager);
//		
//		//
//		// Here is how to install a JSON document content filter 
//		// 
//		JsonStoreServiceFactory jsonStoreFactory = factories.getFactory(JsonStoreServiceFactory.class);
//		jsonStoreFactory.setDocumentContentFilter(new DocumentContentFilter() {
//			@Override
//			public void filter(Document doc) throws JsonException {
//				// You can filter here the JSON stored in the doc before it is sent to the client
//			}
//			@Override
//			public void update(Document doc, Object json) throws JsonException {
//				// You can merge the actual content document with the one sent by the service
//				// Default is to replace the existing content
//				doc.updateJson(json);
//			}
//		});
//	}
	
	/**
	 * Add the application specific services. 
	 */
	@Override
	protected void addApplicationServiceFactories(HttpServiceFactories factories) {
		// The service should always executed locally when running on a server
		factories.add(new AppServiceFactory());
	}
}
