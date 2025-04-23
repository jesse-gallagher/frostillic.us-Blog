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

import java.security.Principal;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.SecurityContext;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;

@RequestScoped
@Named("userInfo")
public class UserInfoBean {
	public static final String ROLE_ADMIN = "[Admin]"; //$NON-NLS-1$

	@Inject @Named("dominoSession")
	private Session session;
	
	@Inject
	private SecurityContext securityContext;

	public String getImageUrl(final String userName) {
//		String md5 = StringUtil.md5Hex(StringUtil.toString(userName).toLowerCase());
//		return StringUtil.format(DarwinoHttpConstants.SOCIAL_USERS_PATH + "/users/{0}/content/photo", URLEncoder.encode(md5, "UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
		// TODO add GravatarProvider from OpenNTF?
		return null;
	}

	public boolean isAdmin() {
		return securityContext.isUserInRole(ROLE_ADMIN);
	}

	public boolean isAnonymous() {
		Principal principal = securityContext.getUserPrincipal();
		return principal == null || "Anonymous".equalsIgnoreCase(principal.getName());
	}

	public String getCn() {
		Principal principal = securityContext.getUserPrincipal();
		try {
			if(principal != null) {
				Name name = session.createName(principal.getName());
				try {
					return name.getCommon();
				} finally {
					name.recycle();
				}
			} else {
				return "Anonymous";
			}
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	public String getDn() {
		Principal principal = securityContext.getUserPrincipal();
		try {
			if(principal != null) {
				Name name = session.createName(principal.getName());
				try {
					return name.getCanonical();
				} finally {
					name.recycle();
				}
			} else {
				return "Anonymous";
			}
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	public String getEmailAddress() {
		// TODO use email provider?
		return "";
	}
}
