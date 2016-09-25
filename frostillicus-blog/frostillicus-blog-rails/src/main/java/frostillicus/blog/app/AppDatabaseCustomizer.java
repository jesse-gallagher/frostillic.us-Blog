/*!COPYRIGHT HEADER! 
 *
 */

package frostillicus.blog.app;

import com.darwino.jsonstore.sql.impl.full.JdbcDatabaseCustomizer;
import com.darwino.sql.drivers.DBDriver;



/**
 * Database customizer.
 */
public class AppDatabaseCustomizer extends JdbcDatabaseCustomizer {
	
	public static final int VERSION = 0;
	
	public AppDatabaseCustomizer(DBDriver driver) {
		super(driver,null);
	}
	
	@Override
	public int getVersion(String databaseName) {
		return VERSION ;
	}

//	@Override
//	public void getAlterStatements(List<String> statements, String schema, String databaseName, int existingVersion) throws JsonException {
//		if(existingVersion==VERSION) {
//			// Ok, we are good!
//			return;
//		}
//	}
}
