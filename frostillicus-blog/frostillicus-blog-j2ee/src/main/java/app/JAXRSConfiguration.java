/*
 * Copyright © 2012-2022 Jesse Gallagher
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
package app;

import java.util.Map;

import org.eclipse.krazo.Properties;

import jakarta.mvc.security.Csrf;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class JAXRSConfiguration extends Application {
	@Override
	public Map<String, Object> getProperties() {
		return Map.of(
			Csrf.CSRF_PROTECTION, Csrf.CsrfOptions.OFF,
			Properties.HIDDEN_METHOD_FILTER_ACTIVE, true
		);
	}
}
