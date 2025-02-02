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
package darwino;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.platform.ManagedBeansService;
import com.darwino.j2ee.application.DarwinoJ2EEApplication;
import com.darwino.platform.DarwinoApplication;
import com.darwino.platform.DarwinoManifest;

import jakarta.servlet.ServletContext;

/**
 * J2EE application.
 */
public class AppJ2EEApplication extends DarwinoJ2EEApplication {

	public static DarwinoJ2EEApplication create(final ServletContext context) throws JsonException {
		if(!DarwinoApplication.isInitialized()) {
			var app = new AppJ2EEApplication(
					context,
					new AppManifest(new AppJ2EEManifest())
			);
			app.init();
		}
		return DarwinoJ2EEApplication.get();
	}

	protected AppJ2EEApplication(final ServletContext context, final DarwinoManifest manifest) {
		super(context,manifest);
	}

	@Override
	public String[] getConfigurationBeanNames() {
		return new String[] {"frostillicus_blog",ManagedBeansService.LOCAL_NAME,ManagedBeansService.DEFAULT_NAME}; //$NON-NLS-1$
	}

}
