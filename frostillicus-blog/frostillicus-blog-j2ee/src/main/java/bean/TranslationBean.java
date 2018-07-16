package bean;

import java.util.ResourceBundle;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

// TODO switch to app scoped and use request locale
@RequestScoped
public class TranslationBean {
	@Produces @Named("translation")
	public ResourceBundle getTranslation() {
		return ResourceBundle.getBundle("translation");
	}
}
