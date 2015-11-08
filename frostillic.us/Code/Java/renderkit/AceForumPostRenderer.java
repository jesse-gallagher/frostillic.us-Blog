package renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.component.data.UIForumPost;
import com.ibm.xsp.extlib.renderkit.html_extended.data.ForumPostRenderer;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.util.FacesUtil;

public class AceForumPostRenderer extends ForumPostRenderer {

	@Override
	protected Object getProperty(final int prop) {
		switch(prop) {
		case PROP_MAINCLASS:
			return "itemdiv dialogdiv"; //$NON-NLS-1$
		case PROP_MAINSTYLE:
			return ""; //$NON-NLS-1$
		case PROP_AUTHORCLASS:
			return "user"; //$NON-NLS-1$
		case PROP_AUTHORSTYLE:
			return ""; //$NON-NLS-1$
		case PROP_POSTCLASS:
			return "body"; //$NON-NLS-1$
		case PROP_POSTSTYLE:
			return ""; //$NON-NLS-1$
		case PROP_AUTHORNAMECLASS:
			return "name"; //$NON-NLS-1$
		case PROP_AUTHORNAMESTYLE:
			return ""; //$NON-NLS-1$
		case PROP_AUTHORMETACLASS:
			return "time"; //$NON-NLS-1$
		case PROP_AUTHORMETASTYLE:
			return ""; //$NON-NLS-1$
		case PROP_POSTDETAILSCLASS:
			return "text"; //$NON-NLS-1$
		case PROP_POSTDETAILSSTYLE:
			return ""; //$NON-NLS-1$
		}
		return super.getProperty(prop);
	}

	@Override
	protected void writeForumPost(final FacesContext context, final ResponseWriter w, final UIForumPost c) throws IOException {
		w.startElement("div", c); //$NON-NLS-1$
		// styleClass
		String userStyleClass = c.getStyleClass();
		String defaultStyleClass = (String)getProperty(PROP_MAINCLASS);
		String styleClass = ExtLibUtil.concatStyleClasses(userStyleClass, defaultStyleClass);
		if(StringUtil.isNotEmpty(styleClass)) {
			w.writeAttribute("class", styleClass, null); //$NON-NLS-1$
		}
		// style
		String userStyle = c.getStyle();
		String defaultStyle = (String)getProperty(PROP_MAINSTYLE);
		String style = ExtLibUtil.concatStyles(userStyle, defaultStyle);
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null); //$NON-NLS-1$
		}

		newLine(w);

		writeAuthor(context, w, c);
		writePost(context, w, c);

		w.endElement("div"); // div.itemdiv.dialogdiv //$NON-NLS-1$
		newLine(w);
	}

	/**
	 * Due to the nature of the theme's structure where user info is in both "user" and "body", this opens a div that is later closed by the content. It's weird.
	 */
	@Override
	protected void writeAuthor(final FacesContext context, final ResponseWriter w, final UIForumPost c) throws IOException {
		UIComponent avatar = c.getFacet(UIForumPost.FACET_AUTHAVATAR);
		UIComponent name = c.getFacet(UIForumPost.FACET_AUTHNAME);
		UIComponent meta = c.getFacet(UIForumPost.FACET_AUTHMETA);

		if(avatar != null) {
			w.startElement("div", null); // div.user //$NON-NLS-1$
			String styleClass = (String)getProperty(PROP_AUTHORCLASS);
			if(StringUtil.isNotEmpty(styleClass)) {
				w.writeAttribute("class", styleClass, null); //$NON-NLS-1$
			}
			String style = (String)getProperty(PROP_AUTHORSTYLE);
			if(StringUtil.isNotEmpty(style)) {
				w.writeAttribute("style", style, null); //$NON-NLS-1$
			}

			writeAuthorAvatar(context, w, c, avatar);

			w.endElement("div"); //$NON-NLS-1$
			newLine(w);
		}

		w.startElement("div", null); // div.body //$NON-NLS-1$
		String styleClass = (String)getProperty(PROP_POSTCLASS);
		if(StringUtil.isNotEmpty(styleClass)) {
			w.writeAttribute("class", styleClass, null); //$NON-NLS-1$
		}
		String style = (String)getProperty(PROP_POSTSTYLE);
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null); //$NON-NLS-1$
		}

		if(meta != null) {
			writeAuthorMeta(context, w, c, meta);
		}
		if(name != null) {
			writeAuthorName(context, w, c, name);
		}
	}

	@Override
	protected void writeAuthorAvatar(final FacesContext context, final ResponseWriter w, final UIForumPost c, final UIComponent facet) throws IOException {
		FacesUtil.renderComponent(context, facet);
	}

	@Override
	protected void writeAuthorName(final FacesContext context, final ResponseWriter w, final UIForumPost c, final UIComponent facet) throws IOException {
		w.startElement("div", null); // div.name //$NON-NLS-1$
		String styleClass = (String)getProperty(PROP_AUTHORNAMECLASS);
		if(StringUtil.isNotEmpty(styleClass)) {
			w.writeAttribute("class", styleClass, null); //$NON-NLS-1$
		}
		String style = (String)getProperty(PROP_AUTHORNAMESTYLE);
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null); //$NON-NLS-1$
		}

		FacesUtil.renderComponent(context, facet);

		w.endElement("div"); //$NON-NLS-1$
	}
	@Override
	protected void writeAuthorMeta(final FacesContext context, final ResponseWriter w, final UIForumPost c, final UIComponent facet) throws IOException {
		w.startElement("div", null); // div.name //$NON-NLS-1$
		String styleClass = (String)getProperty(PROP_AUTHORMETACLASS);
		if(StringUtil.isNotEmpty(styleClass)) {
			w.writeAttribute("class", styleClass, null); //$NON-NLS-1$
		}
		String style = (String)getProperty(PROP_AUTHORMETASTYLE);
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null); //$NON-NLS-1$
		}

		FacesUtil.renderComponent(context, facet);

		w.endElement("div"); //$NON-NLS-1$
	}

	@Override
	protected void writePost(final FacesContext context, final ResponseWriter w, final UIForumPost c) throws IOException {
		UIComponent details = c.getFacet(UIForumPost.FACET_POSTDETAILS);
		if(details == null) {
			return;
		}
		writePostDetails(context, w, c, details);

		w.endElement("div"); // overarching div //$NON-NLS-1$
	}

	@Override
	protected void writePostDetails(final FacesContext context, final ResponseWriter w, final UIForumPost c, final UIComponent facet) throws IOException {
		w.startElement("div", null); // div.name //$NON-NLS-1$
		String styleClass = (String)getProperty(PROP_POSTDETAILSCLASS);
		if(StringUtil.isNotEmpty(styleClass)) {
			w.writeAttribute("class", styleClass, null); //$NON-NLS-1$
		}
		String style = (String)getProperty(PROP_POSTDETAILSSTYLE);
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null); //$NON-NLS-1$
		}

		FacesUtil.renderComponent(context, facet);

		w.endElement("div"); //$NON-NLS-1$
	}
}
