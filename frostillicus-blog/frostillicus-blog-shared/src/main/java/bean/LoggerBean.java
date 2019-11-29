package bean;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import darwino.AppDatabaseDef;

/**
 * Provides an app logger for CDI injection.
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@ApplicationScoped
public class LoggerBean {
	@Produces
	public Logger getLogger() {
		return Logger.getLogger(AppDatabaseDef.DATABASE_NAME);
	}
}
