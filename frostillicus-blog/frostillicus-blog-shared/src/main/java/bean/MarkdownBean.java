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
package bean;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlWriter;

import lombok.SneakyThrows;

@ApplicationScoped
public class MarkdownBean {
	public static interface SyntaxHighlighter {
		String highlight(String text, String language);
	}

	/**
	 * Singleton instance for use outside of CDI environments.
	 */
	public static final MarkdownBean INSTANCE = new MarkdownBean();

	private Parser markdown = Parser.builder().build();
	private HtmlRenderer markdownHtml = HtmlRenderer.builder()
			.nodeRendererFactory(SyntaxHighligherRenderer::new)
			.build();

	@Inject
	private SyntaxHighlighter syntaxHighlighter;

	public String toHtml(final String text) {
		Node parsed = markdown.parse(text);
		return markdownHtml.render(parsed);
	}

	private class SyntaxHighligherRenderer extends AbstractVisitor implements NodeRenderer {
		private final HtmlNodeRendererContext context;

		public SyntaxHighligherRenderer(final HtmlNodeRendererContext context) {
			this.context = context;
		}

		@Override
		public Set<Class<? extends Node>> getNodeTypes() {
			return Collections.singleton(FencedCodeBlock.class);
		}

		@Override
		public void render(final Node node) {
			node.accept(this);
		}

		@Override
		@SneakyThrows
		public void visit(final FencedCodeBlock fencedCodeBlock) {
			HtmlWriter html = context.getWriter();
			html.line();

			String literal = fencedCodeBlock.getLiteral();
			Map<String, String> attributes = new LinkedHashMap<>();
			String info = fencedCodeBlock.getInfo();
			if (info != null && !info.isEmpty()) {
				int space = info.indexOf(" "); //$NON-NLS-1$
				String language;
				if (space == -1) {
					language = info;
				} else {
					language = info.substring(0, space);
				}

				html.tag("div", Collections.singletonMap("class", "hilite-me")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				html.raw(syntaxHighlighter.highlight(literal, language));
				html.tag("/div"); //$NON-NLS-1$
			} else {
				html.tag("pre", getAttrs(fencedCodeBlock, "pre")); //$NON-NLS-1$ //$NON-NLS-2$
				html.tag("code", getAttrs(fencedCodeBlock, "code", attributes)); //$NON-NLS-1$ //$NON-NLS-2$
				html.text(literal);
				html.tag("/code"); //$NON-NLS-1$
				html.tag("/pre"); //$NON-NLS-1$
			}

			html.line();
		}

		private Map<String, String> getAttrs(final Node node, final String tagName) {
			return getAttrs(node, tagName, Collections.<String, String>emptyMap());
		}

		private Map<String, String> getAttrs(final Node node, final String tagName, final Map<String, String> defaultAttributes) {
			return context.extendAttributes(node, tagName, defaultAttributes);
		}
	}
}
