package security;

import java.security.Principal;

import com.darwino.commons.security.acl.User;

/**
 * Adapter for the Java EE 8 Security API 1.0 to work with a Darwino backend.
 * 
 * @author Jesse Gallagher
 */
public class DarwinoPrincipal implements Principal {
	private final User user;
	
	public DarwinoPrincipal(User user) {
		this.user = user;
	}

	@Override
	public String getName() {
		return user.getDn();
	}

}
