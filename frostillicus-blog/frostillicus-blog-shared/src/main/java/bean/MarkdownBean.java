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

import javax.enterprise.context.ApplicationScoped;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

@ApplicationScoped
public class MarkdownBean {
	/**
	 * Singleton instance for use outside of CDI environments.
	 */
	public static final MarkdownBean INSTANCE = new MarkdownBean();

	private Parser markdown = Parser.builder().build();
	private HtmlRenderer markdownHtml = HtmlRenderer.builder().build();
	
	public String toHtml(String text) {
		Node parsed = markdown.parse(text);
		return markdownHtml.render(parsed);
	}
}
