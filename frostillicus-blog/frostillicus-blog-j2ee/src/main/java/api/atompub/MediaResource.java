package api.atompub;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.DateTimeISO8601;
import com.darwino.commons.util.PathUtil;
import com.darwino.commons.util.io.content.ByteArrayContent;
import com.darwino.commons.xml.DomUtil;
import com.darwino.jsonstore.Attachment;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Document;
import com.darwino.jsonstore.Store;

import bean.UserInfoBean;
import darwino.AppDatabaseDef;
import lombok.SneakyThrows;
import org.w3c.dom.Element;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

@Path(AtomPubAPI.BASE_PATH + "/{blogId}/" + MediaResource.PATH)
@RolesAllowed(UserInfoBean.ROLE_ADMIN)
public class MediaResource {
    public static final String PATH = "media";

    @Inject
    @Named("translation")
    ResourceBundle translation;

    @Context
    UriInfo uriInfo;
    
    @Context
    HttpServletRequest request;

    @Inject
    Database database;

    @GET
    @Produces("application/atom+xml")
    public String list() throws JsonException {
        org.w3c.dom.Document xml = DomUtil.createDocument();
        Element feed = DomUtil.createRootElement(xml, "feed");
        feed.setAttribute("xmlns", "http://www.w3.org/2005/Atom");

        Store store = database.getStore(AppDatabaseDef.STORE_MEDIA);
        store.openCursor().findDocuments(doc -> {
            Element entry = DomUtil.createElement(feed, "entry");
            entry.setAttribute("xmlns", "http://www.w3.org/2005/Atom");
            populateAtomXml(entry, doc);
            return true;
        });

        return DomUtil.getXMLString(xml);
    }

    @POST
    @Produces("application/atom+xml")
    public Response uploadMedia(byte[] media) throws JsonException, URISyntaxException {
        String contentType = request.getContentType();
        String name = request.getHeader("Slug");

        // TODO make sure it's not already there
        // This could use the name as the UNID, but it's kind of nice having a "real" UNID behind the scenes
        Store store = database.getStore(AppDatabaseDef.STORE_MEDIA);
        Document doc = store.newDocument();
        doc.set("name", name);
        doc.createAttachment(name, new ByteArrayContent(media, contentType));
        doc.save();

        return Response.created(new URI(resolveUrl(AtomPubAPI.BLOG_ID, PATH, doc.getUnid()))).entity(toAtomXml(doc)).build();
    }

    @GET
    @Path("{mediaId}")
    @Produces("application/atom+xml")
    public Response getMediaInfo(@PathParam("mediaId") String mediaId) throws JsonException {
        Store store = database.getStore(AppDatabaseDef.STORE_MEDIA);
        Document doc = store.loadDocument(mediaId);
        return Response.ok(toAtomXml(doc)).build();
    }

    @GET
    @Path("{mediaId}/{name}")
    public Response getMedia(@PathParam("mediaId") String mediaId) throws JsonException {
        Store store = database.getStore(AppDatabaseDef.STORE_MEDIA);
        Document doc = store.loadDocument(mediaId);

        Attachment att = doc.getAttachments()[0];

        return Response.ok(att.getInputStream()).header(HttpHeaders.CONTENT_TYPE, att.getMimeType()).build();
    }

    private String toAtomXml(Document doc) {
        org.w3c.dom.Document xml = DomUtil.createDocument();
        Element entry = DomUtil.createRootElement(xml, "entry");
        entry.setAttribute("xmlns", "http://www.w3.org/2005/Atom");
        populateAtomXml(entry, doc);
        return DomUtil.getXMLString(xml);
    }

    @SneakyThrows
    private void populateAtomXml(Element entry, Document doc) {
        DomUtil.createElement(entry, "title", doc.getString("name"));
        DomUtil.createElement(entry, "id", doc.getUnid());
        DomUtil.createElement(entry, "updated", DateTimeISO8601.formatISO8601(doc.getLastModificationDate().getTime()));
        Element author = DomUtil.createElement(entry, "author");
        DomUtil.createElement(author, "name", doc.getCreationUser());
        Element summary = DomUtil.createElement(entry, "summary");
        summary.setAttribute("type", "text");

        Attachment att = doc.getAttachments()[0];
        Element content = DomUtil.createElement(entry, "content");
        content.setAttribute("type", att.getMimeType());

        String nameEnc = URLEncoder.encode(doc.getString("name"), StandardCharsets.UTF_8.name());
        String path = PathUtil.concat(MediaResource.PATH, doc.getUnid(), nameEnc);
        content.setAttribute("src", path);

        Element editMediaLink = DomUtil.createElement(entry, "link");
        editMediaLink.setAttribute("edit-media", resolveUrl(AtomPubAPI.BLOG_ID, PATH, doc.getUnid(), nameEnc));

        Element editLink = DomUtil.createElement(entry, "link");
        editLink.setAttribute("rel", "edit");
        editMediaLink.setAttribute("edit-media", resolveUrl(AtomPubAPI.BLOG_ID, PATH, doc.getUnid()));
    }

    private String resolveUrl(String... parts) {
        URI baseUri = uriInfo.getBaseUri();
        String uri = PathUtil.concat(baseUri.toString(), AtomPubAPI.BASE_PATH);
        for(String part : parts) {
            uri = PathUtil.concat(uri, part);
        }
        return uri;
    }
}
