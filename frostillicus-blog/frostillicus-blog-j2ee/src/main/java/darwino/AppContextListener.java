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

import com.darwino.commons.json.JsonException;
import com.darwino.j2ee.application.AbstractDarwinoContextListener;
import com.darwino.j2ee.application.BackgroundServletSynchronizationExecutor;
import com.darwino.j2ee.application.DarwinoJ2EEApplication;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener extends AbstractDarwinoContextListener {

	private BackgroundServletSynchronizationExecutor syncExecutor;

	public AppContextListener() {
	}

	@Override
	protected DarwinoJ2EEApplication createDarwinoApplication(final ServletContext context) throws JsonException {
		return AppJ2EEApplication.create(context);
	}

	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		super.contextInitialized(sce);

 		// Define these to enable the background replication with another server
		syncExecutor = new BackgroundServletSynchronizationExecutor(getApplication(), sce.getServletContext());
		syncExecutor.putPropertyValue("dwo-sync-database",AppDatabaseDef.DATABASE_NAME); //$NON-NLS-1$
		syncExecutor.start();
	}

	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		if(syncExecutor!=null) {
			syncExecutor.stop();
			syncExecutor = null;
		}
		super.contextDestroyed(sce);
	}
}
