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
package bean;

import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Named;

// TODO switch to app scoped and use request locale
@RequestScoped
public class TranslationBean {
	@Produces @Named("translation")
	public ResourceBundle getTranslation() {
		return ResourceBundle.getBundle("translation"); //$NON-NLS-1$
	}
	
	@Produces @Named("messages")
	public Messages getMessages() {
		return Messages.INSTANCE;
	}
	
	public static final class Messages {
		public static final Messages INSTANCE = new Messages();
		
		public String format(String key, Object... params) {
			ResourceBundle translation = CDI.current().select(ResourceBundle.class, NamedLiteral.of("translation")).get(); //$NON-NLS-1$
			String message = translation.getString(key);
			return MessageFormat.format(message, params);
		}

		public String getMonth(int index) {
			return DateFormatSymbols.getInstance().getMonths()[index];
		}
	}
}
