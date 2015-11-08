package renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.component.data.AbstractDataView;
import com.ibm.xsp.extlib.renderkit.html_extended.data.ForumViewRenderer;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.util.FacesUtil;

public class AceForumViewRenderer extends ForumViewRenderer {

	@Override
	protected Object getProperty(final int prop) {
		switch(prop) {
		case PROP_MAINLISTCLASS:
			return "dialogs"; //$NON-NLS-1$
		case PROP_MAINLISTSTYLE:
			return ""; //$NON-NLS-1$
		}
		return super.getProperty(prop);
	}

	/**
	 * The superclass's method is hard-coded to be a UL, which we don't want
	 */
	@Override
	protected void writeContent(final FacesContext context, final ResponseWriter w, final AbstractDataView c, final ViewDefinition viewDef) throws IOException {
		w.startElement("div", c); //$NON-NLS-1$
		// styleClass
		String userStyleClass = c.getStyleClass();
		String defaultStyleClass = (String)getProperty(PROP_MAINLISTCLASS);
		String styleClass = ExtLibUtil.concatStyleClasses(userStyleClass, defaultStyleClass);
		if(StringUtil.isNotEmpty(styleClass)) {
			w.writeAttribute("class", styleClass, null); //$NON-NLS-1$
		}
		// style
		String userStyle = c.getStyle();
		String defaultStyle = (String)getProperty(PROP_MAINLISTSTYLE);
		String style = ExtLibUtil.concatStyles(userStyle, defaultStyle);
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null); //$NON-NLS-1$
		}

		newLine(w);

		// And the rows
		int first = c.getFirst();
		int count = c.getRows();
		writeRows(context, w, c, viewDef, first, count);


		w.endElement("div"); // div.dialogs //$NON-NLS-1$
		newLine(w);
	}

	@Override
	protected void writeStandardRow(final FacesContext context, final ResponseWriter w, final AbstractDataView c, final ViewDefinition viewDef) throws IOException {
		startItem(context, w, c, viewDef, true);

		writeStandardContent(context, w, c, viewDef);

		endItem(context, w, c, viewDef);
	}

	@Override
	protected void startItem(final FacesContext context, final ResponseWriter w, final AbstractDataView c, final ViewDefinition viewDef, final boolean emitId) throws IOException {
		//		w.startElement("div", null);

	}
	@Override
	protected void endItem(final FacesContext context, final ResponseWriter w, final AbstractDataView c, final ViewDefinition viewDef) throws IOException {
		//		w.endElement("div");
		newLine(w);
	}

	@Override
	protected void writeDetail(final FacesContext context, final ResponseWriter w, final AbstractDataView c, final ViewDefinition viewDef) throws IOException {
		if(!viewDef.hasDetail) {
			return;
		}
		// If the detail should not be displayed, then leave
		boolean detailVisible = viewDef.rowDetailVisible;
		if(!detailVisible && !viewDef.detailsOnClient) {
			return;
		}

		UIComponent detail = viewDef.detailFacet;
		if(detail != null) {
			FacesUtil.renderComponent(context, detail);
		}
	}
}
