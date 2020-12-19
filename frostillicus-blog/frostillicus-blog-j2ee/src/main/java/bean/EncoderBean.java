/*
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
package bean;

import java.net.URLEncoder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.darwino.commons.util.StringUtil;

import lombok.SneakyThrows;

/**
 * This bean is intended to be a JSP utility bean for text encoding.
 *
 * @since 2.2.0
 */
@ApplicationScoped @Named("encoder")
public class EncoderBean {

	/**
	 * URL-encodes the provided value, using {@link URLEncoder#encode(String, String)}.
	 *
	 * @param value the value to URL-encode
	 * @return the URL-encoded value
	 */
	@SneakyThrows
	public String urlEncode(final String value) {
		if(StringUtil.isEmpty(value)) {
			return StringUtil.EMPTY_STRING;
		} else {
			return URLEncoder.encode(value, StringUtil.UTF_8.name());
		}
	}
}
