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

import com.darwino.commons.json.JsonArray;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.json.query.parser.BaseParser;
import com.darwino.commons.util.StringUtil;
import com.darwino.jsonstore.extensions.DefaultExtensionRegistry;
import com.darwino.jsonstore.impl.DarwinoInfCursorFactory;
import com.darwino.jsonstore.local.DefaultDatabaseACLFactory;

import bean.UserInfoBean;
import model.Post;

/**
 * Database Business logic - event handlers.
 */
public  class AppDBBusinessLogic extends DefaultExtensionRegistry {
	
	public AppDBBusinessLogic() {
		setQueryFactory(new DarwinoInfCursorFactory(getClass()));
		setDatabaseACLFactory(new DefaultDatabaseACLFactory());
		
		setDynamicSecurity((database, store) -> {
			// Hide all conflict documents outright
			JsonObject result = JsonObject.of("isConflict", false); //$NON-NLS-1$
			
			// Require admin access for draft posts and spam comments
			switch(StringUtil.toString(store)) {
			case AppDatabaseDef.STORE_POSTS:
				if(!database.getUserContext().hasRole(UserInfoBean.ROLE_ADMIN)) {
					result = JsonObject.of(BaseParser.Op.AND.getValue(), JsonArray.of(
						result,
						JsonObject.of("status", //$NON-NLS-1$
							JsonObject.of(BaseParser.Op.NE.getValue(), Post.Status.Draft.name())
						)
					));
				}
				break;
			case AppDatabaseDef.STORE_COMMENTS:
				if(!database.getUserContext().hasRole(UserInfoBean.ROLE_ADMIN)) {
					result = JsonObject.of(BaseParser.Op.AND.getValue(), JsonArray.of(
						result,
						JsonObject.of("akismetspam", //$NON-NLS-1$
								JsonObject.of(BaseParser.Op.NE.getValue(), true)
							)
					));
				}
				break;
			}
			return result.toString();
		});
	}
}
