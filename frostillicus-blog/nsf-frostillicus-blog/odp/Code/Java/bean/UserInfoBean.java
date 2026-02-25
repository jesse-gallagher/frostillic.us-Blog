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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.ibm.commons.util.StringUtil;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mvc.MvcContext;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.xml.bind.DatatypeConverter;
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

	@Inject
	private MvcContext mvc;

	public String getImageUrl(final String userName) {
		var email = getEmailAddress(userName);
		if(StringUtil.isEmpty(email)) {
			email = userName;
		}

		try {
			// If found, send them to Gravatar
			var md = MessageDigest.getInstance("MD5");
		    md.update(email.getBytes());
		    var digest = md.digest();
		    var md5 = DatatypeConverter.printHexBinary(digest).toLowerCase();
		    return mvc.getBasePath() + "/userPhoto/" + md5;
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isAdmin() {
		return securityContext.isUserInRole(ROLE_ADMIN);
	}

	public boolean isAnonymous() {
		var principal = securityContext.getUserPrincipal();
		return principal == null || "Anonymous".equalsIgnoreCase(principal.getName());
	}

	public String getCn() {
		var principal = securityContext.getUserPrincipal();
		try {
			if(principal != null) {
				var name = session.createName(principal.getName());
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
		var principal = securityContext.getUserPrincipal();
		try {
			if(principal != null) {
				var name = session.createName(principal.getName());
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
		var principal = securityContext.getUserPrincipal();
		if(principal == null || "anonymous".equalsIgnoreCase(principal.getName())) {
			return "";
		}

		return getEmailAddress(principal.getName());
	}

	private String getEmailAddress(String userName) {
		if(StringUtil.isEmpty(userName)) {
			return "";
		}

		try {
			var session = CDI.current().select(Session.class, NamedLiteral.of("dominoSession")).get();
			var dir = session.getDirectory();
			var nav = dir.lookupNames("($Users)", userName, "InternetAddress");
			if(nav.findFirstMatch()) {
				List<?> vals = nav.getFirstItemValue();
				if(vals != null && !vals.isEmpty()) {
					return StringUtil.toString(vals.get(0));
				}
			}
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}

		return "";
	}
}
