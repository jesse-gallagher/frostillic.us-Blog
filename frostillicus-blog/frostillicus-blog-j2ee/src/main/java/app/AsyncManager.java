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
		public DarwinoThread(Runnable r) {
			super(r);
		}
		
		@Override
		@SneakyThrows
		public void run() {
			User user = new UserImpl("_SystemUser_", "System User", null, null); //$NON-NLS-1$ //$NON-NLS-2$
			DarwinoJ2EEContext ctx = new DarwinoJ2EEContext(DarwinoJreApplication.get(), null, null, user, new UserContextFactory(), null, DarwinoJreApplication.get().getLocalJsonDBServer().createSystemSession(null));
			DarwinoJ2EEContextFactory fac = (DarwinoJ2EEContextFactory)Platform.getService(DarwinoContextFactory.class);
			fac.push(ctx);
			try {
				super.run();
			} finally {
				fac.pop();
			}
		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		executor.shutdown();
	}
}
