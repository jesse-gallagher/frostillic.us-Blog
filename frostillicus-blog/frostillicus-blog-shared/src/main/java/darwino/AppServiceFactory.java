/**
 * Copyright © 2012-2019 Jesse Gallagher
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

import java.util.List;

import com.darwino.commons.json.JsonArray;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.services.HttpService;
import com.darwino.commons.services.HttpServiceContext;
import com.darwino.commons.services.HttpServiceError;
import com.darwino.commons.services.rest.RestServiceBinder;
import com.darwino.commons.services.rest.RestServiceFactory;
import com.darwino.commons.util.Lic;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Session;
import com.darwino.platform.DarwinoApplication;
import com.darwino.platform.DarwinoContext;
import com.darwino.platform.DarwinoHttpConstants;


/**
 * Application Service Factory.
 * 
 * This is the place where to define custom application services.
 */
public class AppServiceFactory extends RestServiceFactory {
	
	public class AppInformation extends HttpService {
		@Override
		public void service(HttpServiceContext context) {
			if(context.isGet()) {
				JsonObject o = new JsonObject();
				try {
					o.put("name", "frostillicus_blog"); //$NON-NLS-1$ //$NON-NLS-2$
					
					// Access to the app manifest
					AppManifest mf = (AppManifest)DarwinoApplication.get().getManifest();
					o.put("application", DarwinoApplication.get().toString() ); //$NON-NLS-1$
					o.put("label", mf.getLabel()); //$NON-NLS-1$
					o.put("description", mf.getDescription()); //$NON-NLS-1$
					
					// Access to the database session
					JsonObject jSession = new JsonObject();
					Session session = DarwinoContext.get().getSession();
					jSession.put("user", session.getUser().getDn()); //$NON-NLS-1$
					jSession.put("instanceId", session.getInstanceId()); //$NON-NLS-1$
					o.put("session", jSession); //$NON-NLS-1$
					
					addAppInfo(o);
				} catch(Exception ex) {
					o.put("exception", HttpServiceError.exceptionAsJson(ex, false)); //$NON-NLS-1$
				}
				context.emitJson(o);
			} else {
				throw HttpServiceError.errorUnsupportedMethod(context.getMethod());
			}
		}
	}
	
	public class Properties extends HttpService {
		@Override
		public void service(HttpServiceContext context) {
			if(context.isGet()) {
				JsonObject o = new JsonObject();
				try {
					// Check if JSON query is supported by this DB driver
					o.put("jsonQuery", DarwinoApplication.get().getLocalJsonDBServer().isJsonQuerySupported()); //$NON-NLS-1$
					
					// Instances are only supported with the Enterprise edition
					o.put("useInstances", false); //$NON-NLS-1$
					if(Lic.isEnterpriseEdition()) {
						Session session = DarwinoContext.get().getSession();
						String dbName = DarwinoApplication.get().getManifest().getMainDatabase();
						Database db = session.getDatabase(dbName);
						if(db.isInstanceEnabled()) {
							o.put("useInstances", true); //$NON-NLS-1$
							// The instances can be fixed from a property or read from the database
							//JsonArray a = new JsonArray(session.getDatabaseInstances(dbName));
							JsonArray a = new JsonArray(AppDatabaseDef.getInstances());
							o.put("instances", a); //$NON-NLS-1$
						}
					}
				} catch(Exception ex) {
					o.put("exception", HttpServiceError.exceptionAsJson(ex, false)); //$NON-NLS-1$
				}
				context.emitJson(o);
			} else {
				throw HttpServiceError.errorUnsupportedMethod(context.getMethod());
			}
		}
	}
	
	public AppServiceFactory() {
		super(DarwinoHttpConstants.APPSERVICES_PATH);
	}
	
	protected void addAppInfo(JsonObject o) {
		// Add specific app information here..
	}
	
	@Override
	protected void createServicesBinders(List<RestServiceBinder> binders) {
		/////////////////////////////////////////////////////////////////////////////////
		// INFORMATION
		binders.add(new RestServiceBinder() {
			@Override
			public HttpService createService(HttpServiceContext context, String[] parts) {
				return new AppInformation();
			}
		});
		
		/////////////////////////////////////////////////////////////////////////////////
		// APPLICATION PROPERTIES
		binders.add(new RestServiceBinder("properties") { //$NON-NLS-1$
			@Override
			public HttpService createService(HttpServiceContext context, String[] parts) {
				return new Properties();
			}
		});
	}	
}
