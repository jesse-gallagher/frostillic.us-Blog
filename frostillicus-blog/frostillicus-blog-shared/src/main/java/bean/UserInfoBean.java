/*
 * Copyright (c) 2012-2025 Jesse Gallagher
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

import com.darwino.commons.security.acl.User;
import com.darwino.commons.security.acl.UserException;
import com.darwino.commons.util.StringUtil;
import com.darwino.platform.DarwinoContext;
import com.darwino.platform.DarwinoHttpConstants;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.SneakyThrows;

@RequestScoped
@Named("userInfo")
public class UserInfoBean {
	public static final String ROLE_ADMIN = "admin"; //$NON-NLS-1$

	@Inject @Named("darwinoContext")
	DarwinoContext context;

	@SneakyThrows
	public String getImageUrl(final String userName) {
		String md5 = StringUtil.md5Hex(StringUtil.toString(userName).toLowerCase());
		return StringUtil.format(DarwinoHttpConstants.SOCIAL_USERS_PATH + "/users/{0}/content/photo", URLEncoder.encode(md5, "UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean isAdmin() {
		return context.getUser().hasRole(ROLE_ADMIN);
	}

	public boolean isAnonymous() {
		return context.getUser().isAnonymous();
	}

	public String getCn() {
		return context.getUser().getCn();
	}

	public String getDn() {
		return context.getUser().getDn();
	}

	public String getEmailAddress() throws UserException {
		Object mail = context.getUser().getAttribute(User.ATTR_EMAIL);
		return StringUtil.toString(mail);
	}
}
