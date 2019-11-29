package security;

import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.IdentityStoreHandler;

import com.darwino.commons.Platform;
import com.darwino.commons.security.acl.User;
import com.darwino.commons.security.acl.UserException;
import com.darwino.commons.security.acl.UserService;

import security.AccessTokenAuthHandler.AccessTokenAuthenticator;

/**
 * Implements the Jakarta EE security API for {@link AccessTokenAuthHandler} use.
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@ApplicationScoped
public class AccessTokenIdentityStore implements IdentityStore, IdentityStoreHandler {
	public static final String STORE_ID = AccessTokenIdentityStore.class.getName();

	public CredentialValidationResult validate(AccessTokenAuthenticator credential) {
		UserService userDir = Platform.getService(UserService.class);
		String dn = credential.getDn();
		try {
			User user = userDir.findUser(dn);
			return new CredentialValidationResult(STORE_ID, dn, dn, dn, user.getRoles());
		} catch (UserException e) {
			throw new RuntimeException(e);
		}
	}
	
	public CredentialValidationResult validate(UsernamePasswordCredential credential) {
		UserService userDir = Platform.getService(UserService.class);
		String dn;
		try {
			if((dn = userDir.getAuthenticator().authenticate(credential.getCaller(), credential.getPasswordAsString())) != null) {
				User user = userDir.findUser(dn);
				return new CredentialValidationResult(STORE_ID, credential.getCaller(), dn, dn, user.getRoles());
			}
		} catch (UserException e) {
			throw new RuntimeException(e);
		}
		return CredentialValidationResult.INVALID_RESULT;
	}

	@Override
	public CredentialValidationResult validate(Credential credential) {
		if(credential instanceof AccessTokenAuthenticator) {
			return validate((AccessTokenAuthenticator)credential);
		} else if(credential instanceof UsernamePasswordCredential) {
			return validate((UsernamePasswordCredential)credential);
		}
		return null;
	}
}
