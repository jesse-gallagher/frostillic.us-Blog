package api.atompub;

import api.AtomPubAPI;
import com.darwino.commons.json.JsonArray;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.xml.DomUtil;
import com.darwino.jsonstore.Database;
import frostillicus.blog.app.AppDatabaseDef;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path(AtomPubAPI.BASE_PATH + "/{blogId}/categories")
@RolesAllowed("admin")
public class CategoriesResource {
    @Inject
    Database database;

    @GET
    @Produces("application/atomserv+xml")
    public String list() throws JsonException {
        Document xml = DomUtil.createDocument();
        Element service = DomUtil.createRootElement(xml, "app:categories"); //$NON-NLS-1$
        service.setAttribute("xmlns:app", "http://www.w3.org/2007/app"); //$NON-NLS-1$ //$NON-NLS-2$
        service.setAttribute("xmlns:atom", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$
        service.setAttribute("fixed", "no");

        JsonArray tags = (JsonArray)database.getStore(AppDatabaseDef.STORE_POSTS).getTags(Integer.MAX_VALUE, true);
        tags.stream()
            .map(JsonObject.class::cast)
            .map(tag -> tag.getAsString("name"))
            .forEach(tag -> {
                Element category = DomUtil.createElement(service, "atom:category");
                category.setAttribute("term", tag);
            }
        );

        return DomUtil.getXMLString(xml);
    }
}
