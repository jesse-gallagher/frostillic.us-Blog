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
package darwino;

import com.darwino.commons.services.HttpServiceFactories;
import com.darwino.commons.services.debug.DebugRestFactory;
import com.darwino.j2ee.application.DarwinoJ2EEServiceDispatcherFilter;

import javax.servlet.DispatcherType;
import javax.servlet.annotation.WebFilter;

/**
 * Service dispatcher.
 */
@WebFilter(filterName="DarwinoServices", urlPatterns="/*", dispatcherTypes={DispatcherType.REQUEST, DispatcherType.FORWARD})
public class AppServiceDispatcher extends DarwinoJ2EEServiceDispatcherFilter {
	
	public AppServiceDispatcher() {
	}
	
	/**
	 * Add the application specific services. 
	 */
	@Override
	protected void addApplicationServiceFactories(HttpServiceFactories factories) {
		// The service should always executed locally when running on a server
		factories.add(new AppServiceFactory());
		
		// Add the debug services
		final DebugRestFactory debug = new DebugRestFactory();  
		factories.add(debug);
	}
	
	@Override
	protected void addJsonStoreServiceFactories(HttpServiceFactories factories) {
		// disabled
	}
	@Override
	protected void addGraphQLServiceFactories(HttpServiceFactories factories) {
		// disabled
	}
	@Override
	protected void addApiServiceFactories(HttpServiceFactories factories) {
		// disabled
	}
	@Override
	protected void addLibrariesServiceFactories(HttpServiceFactories factories) {
		// disabled
	}
	
}
