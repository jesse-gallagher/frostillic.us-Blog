/**
 * Copyright © 2012-2020 Jesse Gallagher
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
