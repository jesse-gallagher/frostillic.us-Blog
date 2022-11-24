/*
 * Copyright © 2012-2022 Jesse Gallagher
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

import com.darwino.commons.Platform;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.StringUtil;
import com.darwino.jsonstore.Base;
import com.darwino.jsonstore.impl.DatabaseFactoryImpl;
import com.darwino.jsonstore.meta._Database;
import com.darwino.jsonstore.meta._DatabaseACL;
import com.darwino.jsonstore.meta._FtSearch;
import com.darwino.jsonstore.meta._Store;

import bean.UserInfoBean;

public class AppDatabaseDef extends DatabaseFactoryImpl {

	public static final int DATABASE_VERSION	= 17;
	public static final String DATABASE_NAME	= "frostillicus_blog"; //$NON-NLS-1$
	public static final String STORE_POSTS = "posts"; //$NON-NLS-1$
	public static final String STORE_COMMENTS = "comments"; //$NON-NLS-1$
	public static final String STORE_CONFIG = "config"; //$NON-NLS-1$
	public static final String STORE_MEDIA = "media"; //$NON-NLS-1$
	/** @since 2.3.0 */
	public static final String STORE_MICROPOSTS = "microposts"; //$NON-NLS-1$
	/** @since 2.3.0 */
	public static final String STORE_TOKENS = "tokens"; //$NON-NLS-1$
	/** @since 2.3.0 */
	public static final String STORE_WEBMENTIONS = "webmentions"; //$NON-NLS-1$

	// The list  of instances is defined through a property for the DB
	public static String[] getInstances() {
		String inst = Platform.getProperty("frostillicus_blog.instances"); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(inst)) {
			return StringUtil.splitString(inst, ',', true);
		}
		return null;
	}

	@Override
	public int getDatabaseVersion(final String databaseName) throws JsonException {
		if(!StringUtil.equalsIgnoreCase(databaseName, DATABASE_NAME)) {
			return -1;
		}
		return DATABASE_VERSION;
	}

	@Override
	public _Database loadDatabase(final String databaseName) throws JsonException {
		if(!StringUtil.equalsIgnoreCase(databaseName, DATABASE_NAME)) {
			return null;
		}
		_Database db = new _Database(DATABASE_NAME, "frostillic.us Blog", DATABASE_VERSION); //$NON-NLS-1$
		db.setDocumentSecurity(Base.DOCSEC_NOTESLIKE | Base.DOCSEC_INCLUDE | Base.DOCSEC_DYNAMIC);

		_DatabaseACL acl = new _DatabaseACL();
		acl.addRole(UserInfoBean.ROLE_ADMIN, _DatabaseACL.ROLE_MANAGE);
		acl.addAnonymous(_DatabaseACL.ROLE_AUTHOR);
		acl.addUser("anonymous", _DatabaseACL.ROLE_READER); //$NON-NLS-1$
		db.setACL(acl);

		db.setReplicationEnabled(true);
		db.setDocumentLockEnabled(false);

		db.setInstanceEnabled(false);

		{
			_Store posts = db.addStore(STORE_POSTS);
			posts.setFtSearchEnabled(true);
			posts.setTaggingEnabled(true);
			_FtSearch ft = posts.setFTSearch(new _FtSearch());
			ft.setFields("$"); //$NON-NLS-1$
		}
		{
			_Store comments = db.addStore(STORE_COMMENTS);
			comments.setFtSearchEnabled(true);
			_FtSearch ft = comments.setFTSearch(new _FtSearch());
			ft.setFields("$"); //$NON-NLS-1$
		}
		db.addStore(STORE_CONFIG);
		db.addStore(STORE_MEDIA);
		db.addStore(STORE_MICROPOSTS);
		db.addStore(STORE_TOKENS);
		db.addStore(STORE_WEBMENTIONS);

		return db;
	}
}
