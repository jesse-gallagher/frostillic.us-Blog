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
