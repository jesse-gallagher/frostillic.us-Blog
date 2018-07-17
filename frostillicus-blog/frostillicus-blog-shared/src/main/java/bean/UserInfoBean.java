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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.darwino.commons.security.acl.User;
import com.darwino.commons.security.acl.UserException;
import com.darwino.platform.DarwinoContext;

import util.MD5Util;

@RequestScoped
@Named("userInfo")
public class UserInfoBean {
	@Inject @Named("darwinoContext")
	DarwinoContext context;
	
	public String getGravatarUrl(String emailAddress, int size) {
		return "https://secure.gravatar.com/avatar/" + MD5Util.md5Hex(emailAddress) + "?s=" + size;
	}
	
	public boolean isAdmin() {
		return context.getUser().hasRole("admin");
	}
	
	public boolean isAnonymous() {
		return context.getUser().isAnonymous();
	}
	
	public String getCn() {
		return context.getUser().getCn();
	}
	
	public String getEmailAddress() throws UserException {
		Object mail = context.getUser().getAttribute(User.ATTR_EMAIL);
		return mail == null ? "" : String.valueOf(mail);
	}
}
