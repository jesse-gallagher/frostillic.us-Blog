package api.atompub;

import org.w3c.dom.Document;
import com.darwino.commons.xml.DomUtil;
import org.w3c.dom.Element;

enum AtomPubUtil {
    ;

    public Document createResponse(String tagName) {
        Document result = DomUtil.createDocument();
        Element root = DomUtil.createRootElement(result, tagName);
        root.setAttribute("xmlns", "http://purl.org/atom/app#"); //$NON-NLS-1$ //$NON-NLS-2$
        root.setAttribute("xmlns:atom", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$
        root.setAttribute("xmlns:app", "http://www.w3.org/2007/app"); //$NON-NLS-1$ //$NON-NLS-2$

        return result;
    }
}
