package security;

import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;

import com.darwino.commons.Platform;
import com.darwino.commons.security.acl.User;
import com.darwino.commons.security.acl.UserException;
import com.darwino.commons.security.acl.UserService;

/**
 * Adapter for the Java EE 8 Security API 1.0 to work with a Darwino backend.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class DarwinoIdentityStore implements IdentityStore {
	public static final String STORE_ID = DarwinoIdentityStore.class.getName();
	
	public CredentialValidationResult validate(UsernamePasswordCredential credential) {
		UserService userDir = Platform.getService(UserService.class);
		String dn;
		try {
			if((dn = userDir.getAuthenticator().authenticate(credential.getCaller(), credential.getPasswordAsString())) != null) {
				User user = userDir.findUser(dn);
				return new CredentialValidationResult(STORE_ID, credential.getCaller(), dn, dn, user.getGroups());
			}
		} catch (UserException e) {
			throw new RuntimeException(e);
		}
		return CredentialValidationResult.INVALID_RESULT;
	}
}
