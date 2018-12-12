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

/**
 * Adapter for the Java EE 8 Security API 1.0 to work with a Darwino backend.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
@ApplicationScoped
public class DarwinoIdentityStore implements IdentityStore, IdentityStoreHandler {
	public static final String STORE_ID = DarwinoIdentityStore.class.getName();
	
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
		if(credential instanceof UsernamePasswordCredential) {
			return validate((UsernamePasswordCredential)credential);
		}
		return null;
	}
}
