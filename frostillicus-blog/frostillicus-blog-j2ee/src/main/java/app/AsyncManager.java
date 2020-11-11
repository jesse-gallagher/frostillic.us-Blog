/**
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
package app;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.darwino.commons.Platform;
import com.darwino.commons.security.acl.User;
import com.darwino.commons.security.acl.UserContextFactory;
import com.darwino.commons.security.acl.impl.UserImpl;
import com.darwino.j2ee.application.DarwinoJ2EEContext;
import com.darwino.j2ee.application.DarwinoJ2EEContextFactory;
import com.darwino.jre.application.DarwinoJreApplication;
import com.darwino.platform.DarwinoContextFactory;

import lombok.SneakyThrows;

/**
 * Manages thread executors for async operations.
 *
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public class AsyncManager implements ServletContextListener {
	public static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(DarwinoThread::new);

	public static class DarwinoThread extends Thread {
		public DarwinoThread(final Runnable r) {
			super(r);
		}

		@Override
		@SneakyThrows
		public void run() {
			User user = new UserImpl("_SystemUser_", "System User", null, null); //$NON-NLS-1$ //$NON-NLS-2$
			var ctx = new DarwinoJ2EEContext(DarwinoJreApplication.get(), null, null, user, new UserContextFactory(), null, DarwinoJreApplication.get().getLocalJsonDBServer().createSystemSession(null));
			var fac = (DarwinoJ2EEContextFactory)Platform.getService(DarwinoContextFactory.class);
			fac.push(ctx);
			try {
				super.run();
			} finally {
				fac.pop();
			}
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		executor.shutdown();
	}
}
