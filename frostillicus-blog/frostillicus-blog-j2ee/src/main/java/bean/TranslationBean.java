/*
 * Copyright (c) 2012-2023 Jesse Gallagher
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

import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

@RequestScoped
public class TranslationBean {
	@Inject
	HttpServletRequest request;

	@Produces @Named("translation")
	public ResourceBundle getTranslation() {
		return ResourceBundle.getBundle("translation", request.getLocale()); //$NON-NLS-1$
	}

	@RequestScoped
	@Named("messages")
	public static class Messages {
		@Inject
		HttpServletRequest request;

		public String format(final String key, final Object... params) {
			var translation = CDI.current().select(ResourceBundle.class, NamedLiteral.of("translation")).get(); //$NON-NLS-1$
			var message = translation.getString(key);
			return MessageFormat.format(message, params);
		}

		public String getMonth(final int index) {
			return DateFormatSymbols.getInstance(request.getLocale()).getMonths()[index];
		}
	}
}
