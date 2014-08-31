package config;

import java.io.IOException;
import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.designer.context.XSPContext;
import com.ibm.xsp.model.DataObject;

import frostillicus.xsp.bean.ApplicationScoped;
import frostillicus.xsp.bean.ManagedBean;

@ManagedBean(name="translation")
@ApplicationScoped
public class Translation implements Serializable, DataObject {
	private static final long serialVersionUID = 1L;

	public Class<String> getType(final Object key) {
		return String.class;
	}

	public Object getValue(final Object key) {
		try {
			ResourceBundle bundle = getTranslationBundle();
			return bundle.getString(String.valueOf(key));
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		} catch(MissingResourceException mre) {
			return "[Untranslated " + key + "]";
		}
	}

	public boolean isReadOnly(final Object key) {
		return true;
	}

	public void setValue(final Object key, final Object value) {
		throw new UnsupportedOperationException();
	}


	private ResourceBundle getTranslationBundle() throws IOException {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ApplicationEx app = (ApplicationEx)facesContext.getApplication();
		return app.getResourceBundle("translation", XSPContext.getXSPContext(facesContext).getLocale());
	}
}
