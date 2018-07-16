package bean;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import com.darwino.commons.json.JsonException;
import com.darwino.jsonstore.Session;
import com.darwino.platform.DarwinoContext;

@RequestScoped
public class DarwinoContextBean {
	@Produces @Named("darwinoContext")
	public DarwinoContext getContext() {
		return DarwinoContext.get();
	}
	
	@Produces @Named("darwinoSession")
	public Session getSession() throws JsonException {
		return DarwinoContext.get().getSession();
	}
}
