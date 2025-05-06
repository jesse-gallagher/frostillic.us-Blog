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
import java.security.Principal;
import java.util.List;

import com.ibm.commons.util.StringUtil;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.xml.bind.DatatypeConverter;
import lotus.domino.Directory;
import lotus.domino.DirectoryNavigator;
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
		String email = getEmailAddress(userName);
		if(StringUtil.isEmpty(email)) {
			email = userName;
		}
		try {
			// If found, send them to Gravatar
			MessageDigest md = MessageDigest.getInstance("MD5");
		    md.update(email.getBytes());
		    byte[] digest = md.digest();
		    String md5 = DatatypeConverter.printHexBinary(digest).toLowerCase();
			return "http://www.gravatar.com/avatar/" + md5 + "?d=wavatar&s=256";
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
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
		Principal principal = securityContext.getUserPrincipal();
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
			Session session = CDI.current().select(Session.class, NamedLiteral.of("dominoSession")).get();
			Directory dir = session.getDirectory();
			DirectoryNavigator nav = dir.lookupNames("($Users)", userName, "InternetAddress");
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
