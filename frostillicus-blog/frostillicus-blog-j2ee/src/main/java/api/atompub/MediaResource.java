/**
 * Copyright © 2016-2019 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    public static final String PATH = "media"; //$NON-NLS-1$

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
        Element feed = DomUtil.createRootElement(xml, "feed"); //$NON-NLS-1$
        feed.setAttribute("xmlns", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$

        Store store = database.getStore(AppDatabaseDef.STORE_MEDIA);
        store.openCursor().findDocuments(doc -> {
            Element entry = DomUtil.createElement(feed, "entry"); //$NON-NLS-1$
            entry.setAttribute("xmlns", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$
            populateAtomXml(entry, doc);
            return true;
        });

        return DomUtil.getXMLString(xml);
    }

    @POST
    @Produces("application/atom+xml")
    public Response uploadMedia(byte[] media) throws JsonException, URISyntaxException {
        String contentType = request.getContentType();
        String name = request.getHeader("Slug"); //$NON-NLS-1$

        // TODO make sure it's not already there
        // This could use the name as the UNID, but it's kind of nice having a "real" UNID behind the scenes
        Store store = database.getStore(AppDatabaseDef.STORE_MEDIA);
        Document doc = store.newDocument();
        doc.set("name", name); //$NON-NLS-1$
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
        Element entry = DomUtil.createRootElement(xml, "entry"); //$NON-NLS-1$
        entry.setAttribute("xmlns", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$
        populateAtomXml(entry, doc);
        return DomUtil.getXMLString(xml);
    }

    @SneakyThrows
    private void populateAtomXml(Element entry, Document doc) {
        DomUtil.createElement(entry, "title", doc.getString("name")); //$NON-NLS-1$ //$NON-NLS-2$
        DomUtil.createElement(entry, "id", doc.getUnid()); //$NON-NLS-1$
        DomUtil.createElement(entry, "updated", DateTimeISO8601.formatISO8601(doc.getLastModificationDate().getTime())); //$NON-NLS-1$
        Element author = DomUtil.createElement(entry, "author"); //$NON-NLS-1$
        DomUtil.createElement(author, "name", doc.getCreationUser()); //$NON-NLS-1$
        Element summary = DomUtil.createElement(entry, "summary"); //$NON-NLS-1$
        summary.setAttribute("type", "text"); //$NON-NLS-1$ //$NON-NLS-2$

        Attachment att = doc.getAttachments()[0];
        Element content = DomUtil.createElement(entry, "content"); //$NON-NLS-1$
        content.setAttribute("type", att.getMimeType()); //$NON-NLS-1$

        String nameEnc = URLEncoder.encode(doc.getString("name"), StandardCharsets.UTF_8.name()); //$NON-NLS-1$
        String path = PathUtil.concat(MediaResource.PATH, doc.getUnid(), nameEnc);
        content.setAttribute("src", path); //$NON-NLS-1$

        Element editMediaLink = DomUtil.createElement(entry, "link"); //$NON-NLS-1$
        editMediaLink.setAttribute("edit-media", resolveUrl(AtomPubAPI.BLOG_ID, PATH, doc.getUnid(), nameEnc)); //$NON-NLS-1$

        Element editLink = DomUtil.createElement(entry, "link"); //$NON-NLS-1$
        editLink.setAttribute("rel", "edit"); //$NON-NLS-1$ //$NON-NLS-2$
        editMediaLink.setAttribute("edit-media", resolveUrl(AtomPubAPI.BLOG_ID, PATH, doc.getUnid())); //$NON-NLS-1$
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
