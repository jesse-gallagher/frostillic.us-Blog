/*
 * Copyright (c) 2012-2025 Jesse Gallagher
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

import java.net.URI;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import bean.MarkdownBean.SyntaxHighlighter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import lombok.SneakyThrows;

@ApplicationScoped
public class HiliteMeHighlighter implements SyntaxHighlighter {

	public interface HiliteMeService {
		@POST
		String highlight(
			@FormParam("code") String code,
			@FormParam("lexer") String lexer,
			@FormParam("linenos") boolean lineNos,
			@FormParam("style") String style
		);
	}

	@Override
	@SneakyThrows
	public String highlight(final String text, final String language) {
		// TODO use embedded Python interpreter?
		var apiUri = new URI("http://hilite.me/api"); //$NON-NLS-1$
		var hiliteMe = RestClientBuilder.newBuilder()
			.baseUri(apiUri)
			.build(HiliteMeService.class);
		return hiliteMe.highlight(text, language, true, "colorful"); //$NON-NLS-1$
	}

}
