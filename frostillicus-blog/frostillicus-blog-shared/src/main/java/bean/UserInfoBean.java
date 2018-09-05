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
package bean;

import java.net.URLEncoder;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.darwino.commons.security.acl.User;
import com.darwino.commons.security.acl.UserException;
import com.darwino.commons.util.StringUtil;
import com.darwino.platform.DarwinoContext;

@RequestScoped
@Named("userInfo")
public class UserInfoBean {
	@Inject @Named("darwinoContext")
	DarwinoContext context;
	
	public String getImageUrl(String userName) {
		return StringUtil.format("$darwino-social/users/users/{0}/content/photo", URLEncoder.encode(userName, StringUtil.UTF_8)); //$NON-NLS-1$
	}
	
	public boolean isAdmin() {
		return context.getUser().hasRole("admin"); //$NON-NLS-1$
	}
	
	public boolean isAnonymous() {
		return context.getUser().isAnonymous();
	}
	
	public String getCn() {
		return context.getUser().getCn();
	}
	
	public String getEmailAddress() throws UserException {
		Object mail = context.getUser().getAttribute(User.ATTR_EMAIL);
		return StringUtil.toString(mail);
	}
}
