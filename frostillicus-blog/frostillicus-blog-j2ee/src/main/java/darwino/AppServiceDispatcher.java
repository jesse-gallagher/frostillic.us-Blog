/*
 * Copyright © 2012-2023 Jesse Gallagher
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

import jakarta.servlet.DispatcherType;
import jakarta.servlet.annotation.WebFilter;

import com.darwino.commons.services.HttpServiceFactories;
import com.darwino.commons.services.debug.DebugRestFactory;
import com.darwino.j2ee.application.DarwinoJ2EEServiceDispatcherFilter;

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
	protected void addApplicationServiceFactories(final HttpServiceFactories factories) {
		// Add the debug services
		final var debug = new DebugRestFactory();
		factories.add(debug);
	}

	@Override
	protected void addJsonStoreServiceFactories(final HttpServiceFactories factories) {
		// disabled
	}
	@Override
	protected void addGraphQLServiceFactories(final HttpServiceFactories factories) {
		// disabled
	}
	@Override
	protected void addApiServiceFactories(final HttpServiceFactories factories) {
		// disabled
	}
	@Override
	protected void addLibrariesServiceFactories(final HttpServiceFactories factories) {
		// disabled
	}

}
