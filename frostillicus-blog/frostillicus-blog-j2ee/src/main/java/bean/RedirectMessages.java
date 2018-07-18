package bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Named;
import javax.mvc.annotation.RedirectScoped;


@RedirectScoped
public class RedirectMessages implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final List<String> messages = new ArrayList<>();
	private static final TypeLiteral<List<String>> literal = new TypeLiteral<>() {
		private static final long serialVersionUID = 1L;
	};

	@Produces @Named("redirectMessages")
	public List<String> get() {
		return messages;
	}
	
	public static void add(String message) {
		CDI.current().select(literal, NamedLiteral.of("redirectMessages")).get().add(message);
	}
}
