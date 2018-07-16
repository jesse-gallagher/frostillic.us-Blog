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

import com.darwino.commons.Platform;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.StringUtil;
import com.darwino.jsonstore.impl.DatabaseFactoryImpl;
import com.darwino.jsonstore.meta._Database;
import com.darwino.jsonstore.meta._DatabaseACL;
import com.darwino.jsonstore.meta._FtSearch;
import com.darwino.jsonstore.meta._Store;

public class AppDatabaseDef extends DatabaseFactoryImpl {

	public static final int DATABASE_VERSION	= 6;
	public static final String DATABASE_NAME	= "frostillicus_blog"; //$NON-NLS-1$
	public static final String STORE_POSTS = "posts";
	public static final String STORE_COMMENTS = "comments";
	public static final String STORE_CONFIG = "config";
	
	// The list  of instances is defined through a property for the DB
	public static String[] getInstances() {
		String inst = Platform.getProperty("frostillicus_blog.instances"); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(inst)) {
			return StringUtil.splitString(inst, ',', true);
		}
		return null;
	}	
	
	@Override
	public int getDatabaseVersion(String databaseName) throws JsonException {
		if(!StringUtil.equalsIgnoreCase(databaseName, DATABASE_NAME)) {
			return -1;
		}
		return DATABASE_VERSION;
	}
	
	@Override
	public _Database loadDatabase(String databaseName) throws JsonException {
		if(!StringUtil.equalsIgnoreCase(databaseName, DATABASE_NAME)) {
			return null;
		}
		_Database db = new _Database(DATABASE_NAME, "frostillic.us Blog", DATABASE_VERSION);
		
		_DatabaseACL acl = new _DatabaseACL();
		acl.addRole("admin", _DatabaseACL.ROLE_MANAGE);
		acl.addAnonymous(_DatabaseACL.ROLE_AUTHOR);
		acl.addUser("anonymous", _DatabaseACL.ROLE_AUTHOR);
		db.setACL(acl);

		db.setReplicationEnabled(true);
		
		db.setInstanceEnabled(false);
		
		{
			_Store posts = db.addStore(STORE_POSTS);
			posts.setFtSearchEnabled(true);
			_FtSearch ft = posts.setFTSearch(new _FtSearch());
			ft.setFields("$"); //$NON-NLS-1$
		}
		{
			_Store comments = db.addStore(STORE_COMMENTS);
			comments.setFtSearchEnabled(true);
			_FtSearch ft = comments.setFTSearch(new _FtSearch());
			ft.setFields("$"); //$NON-NLS-1$
		}
		{
			db.addStore(STORE_CONFIG);
		}

		return db;
	}
}
