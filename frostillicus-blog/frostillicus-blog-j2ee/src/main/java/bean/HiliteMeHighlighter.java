package bean;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import bean.MarkdownBean.SyntaxHighlighter;
import lombok.SneakyThrows;

@ApplicationScoped
public class HiliteMeHighlighter implements SyntaxHighlighter {
	
	interface HiliteMeService {
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
	public String highlight(String text, String language) {
		// TODO use embedded Python interpreter?
		URI apiUri = new URI("http://hilite.me/api"); //$NON-NLS-1$
		HiliteMeService hiliteMe = RestClientBuilder.newBuilder()
			.baseUri(apiUri)
			.build(HiliteMeService.class);
		return hiliteMe.highlight(text, language, true, "colorful"); //$NON-NLS-1$
	}

}
