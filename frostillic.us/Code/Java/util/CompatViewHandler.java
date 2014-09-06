package util;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import frostillicus.xsp.controller.ControllingViewHandler;

public class CompatViewHandler extends ControllingViewHandler {

	public CompatViewHandler(final ViewHandler delegate) {
		super(delegate);
	}

	@Override
	public UIViewRoot createView(final FacesContext facesContext, final String viewId) {
		if("/Month".equals(viewId)) {
			// The previous blog used "Month" instead of "month"
			return super.createView(facesContext, "/month");
		} else if("/Search".equals(viewId)) {
			// As above: old style was title-case
			return super.createView(facesContext, "/search");
		}
		return super.createView(facesContext, viewId);
	}
}