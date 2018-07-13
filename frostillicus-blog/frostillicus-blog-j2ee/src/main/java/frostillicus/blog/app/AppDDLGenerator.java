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

import com.darwino.commons.util.UUIDGenerator;
import com.darwino.j2ee.jstore.JdbcDdlGenerator;
import com.darwino.jsonstore.sql.impl.full.JdbcDatabaseCustomizer;
import com.darwino.platform.DarwinoManifest;
import com.darwino.sql.drivers.DBDriver;
import com.darwino.sql.drivers.DBDriverFactory;

/**
 * Application DDL Generator.
 * 
 * Use this class to get the DDL generated and deploy to a database, if the application cannot
 * run the deployment automatically.
 */
public class AppDDLGenerator {
	
	public static void main(String[] args) {
		try {
			// Set the parameters here
			String dbType = DBDriverFactory.DB_POSTGRESQL;
			String dbVersion = null; // Null means default
			String dbSchema = null; // Schema for the tables
			
			// Load the driver for the desired DB here
			DBDriver dbDriver = DBDriverFactory.get().find(dbType,dbVersion);

			DarwinoManifest mf = new AppManifest(null);
			
			// Create the customizer
			JdbcDatabaseCustomizer customizer = new AppDatabaseCustomizer(dbDriver);
			
			// Create the generator
			// The replicaId should be kept the same when the database is upgraded and the replicated data should be kept
			// A newly generated ID will actually restart the replication from scratch
			String replicaId = UUIDGenerator.uuid();
			JdbcDdlGenerator gen = new JdbcDdlGenerator(dbDriver, dbSchema, mf.getDatabaseFactory(), customizer, replicaId);

			// Generate the DDL for all the databases to System.out (Java console)
			gen.generateDdl(System.out,mf.getDatabases());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
