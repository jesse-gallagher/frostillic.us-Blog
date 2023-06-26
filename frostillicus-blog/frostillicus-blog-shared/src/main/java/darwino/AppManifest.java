/*
 * Copyright (c) 2012-2023 Jesse Gallagher
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
import com.darwino.commons.util.StringUtil;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Session;
import com.darwino.jsonstore.extensions.ExtensionRegistry;
import com.darwino.jsonstore.meta.DatabaseFactory;
import com.darwino.platform.DarwinoContext;
import com.darwino.platform.DarwinoManifest;

/**
 * Application Manifest.
 */
public class AppManifest extends DarwinoManifest {

	// This is used by the mobile application to call the remote service
	public static final String MOBILE_PATHINFO	= "/frostillicus-blog"; //$NON-NLS-1$

	public static Session getSession() throws JsonException {
		return DarwinoContext.get().getSession();
	}

	public static Database getDatabase() throws JsonException {
		return getSession().getDatabase(AppDatabaseDef.DATABASE_NAME);
	}


	public AppManifest(final Section section) {
		super(section);
	}

	@Override
	public String getLabel() {
		return "frostillic.us Blog"; //$NON-NLS-1$
	}

	@Override
	public String getDescription() {
		return StringUtil.EMPTY_STRING;
	}

	@Override
	public String getMainPageUrl() {
		return "/"; //$NON-NLS-1$
	}

	@Override
	public String[] getDatabases() {
		return new String[] {
			AppDatabaseDef.DATABASE_NAME,
		};
	}

	@Override
	public DatabaseFactory getDatabaseFactory() {
		return new AppDatabaseDef();
	}

	@Override
	public ExtensionRegistry getExtensionRegistry() {
		return new AppDBBusinessLogic();
	}

	@Override
	public String getConfigId() {
		return AppDatabaseDef.DATABASE_NAME;
	}
}
