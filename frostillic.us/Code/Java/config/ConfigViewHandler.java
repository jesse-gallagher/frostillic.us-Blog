package config;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import util.CompatViewHandler;

public class ConfigViewHandler extends CompatViewHandler {

	public ConfigViewHandler(final ViewHandler arg0) {
		super(arg0);
	}

	@Override
	public UIViewRoot createView(final FacesContext context, final String pageName) {

		if(!AppConfig.get().isComplete()) {
			return super.createView(context, "/appconfig"); //$NON-NLS-1$
		}

		return super.createView(context, pageName);
	}
}
