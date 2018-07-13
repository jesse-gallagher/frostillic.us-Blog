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
