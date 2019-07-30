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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Named;
import javax.mvc.RedirectScoped;


@RedirectScoped
public class RedirectMessages implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final List<String> messages = new ArrayList<>();
	private static final TypeLiteral<List<String>> literal = new TypeLiteral<List<String>>() {
		private static final long serialVersionUID = 1L;
	};

	@Produces @Named("redirectMessages")
	public List<String> get() {
		return messages;
	}
	
	public static void add(String message) {
		CDI.current().select(literal, NamedLiteral.of("redirectMessages")).get().add(message); //$NON-NLS-1$
	}
}
