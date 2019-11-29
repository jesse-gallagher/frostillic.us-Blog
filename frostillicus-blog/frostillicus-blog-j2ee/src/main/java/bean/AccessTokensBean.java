/**
 * Copyright © 2012-2019 Jesse Gallagher
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

import java.util.Collection;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import model.AccessToken;
import model.AccessTokenRepository;

@ApplicationScoped @Named("accessTokens")
public class AccessTokensBean {
	@Inject
	AccessTokenRepository accessTokens;
	
	public Collection<AccessToken> getAll() {
		return accessTokens.findAll().collect(Collectors.toList());
	}
}
