/**
 * Copyright © 2012-2019 Jesse Gallagher
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

import com.darwino.commons.util.DateTimeISO8601;
import com.darwino.commons.util.PathUtil;
import com.darwino.commons.xml.DomUtil;

import bean.UserInfoBean;
import lombok.SneakyThrows;
import model.Media;
import model.MediaRepository;

import org.jnosql.diana.driver.attachment.EntityAttachment;
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
    MediaRepository mediaRepository;

    @GET
    @Produces("application/atom+xml")
    public String list() {
        org.w3c.dom.Document xml = DomUtil.createDocument();
        Element feed = DomUtil.createRootElement(xml, "feed"); //$NON-NLS-1$
        feed.setAttribute("xmlns", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$
        
        mediaRepository.findAll().forEach(m -> {
        	Element entry = DomUtil.createElement(feed, "entry"); //$NON-NLS-1$
            entry.setAttribute("xmlns", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$
            populateAtomXml(entry, m);
        });

        return DomUtil.getXMLString(xml);
    }

    @POST
    @Produces("application/atom+xml")
    public Response uploadMedia(byte[] data) throws URISyntaxException {
        String contentType = request.getContentType();
        String name = request.getHeader("Slug"); //$NON-NLS-1$

        // TODO make sure it's not already there
        // This could use the name as the UNID, but it's kind of nice having a "real" UNID behind the scenes
        Media media = mediaRepository.findByName(name).orElseGet(Media::new);
        media.setName(name);
        media.setAttachments(Arrays.asList(EntityAttachment.of(name, System.currentTimeMillis(), contentType, data)));
        media = mediaRepository.save(media);
        
        // Force update of metadata fields
        media = mediaRepository.findById(media.getId()).get();

        return Response.created(new URI(resolveUrl(AtomPubAPI.BLOG_ID, PATH, media.getId()))).entity(toAtomXml(media)).build();
    }

    @GET
    @Path("{mediaId}")
    @Produces("application/atom+xml")
    public Response getMediaInfo(@PathParam("mediaId") String mediaId) {
    	Media media = mediaRepository.findById(mediaId).orElseThrow(NotFoundException::new);
        return Response.ok(toAtomXml(media)).build();
    }

    @GET
    @Path("{mediaId}/{name}")
    public Response getMedia(@PathParam("mediaId") String mediaId) throws IOException {
    	Media media = mediaRepository.findById(mediaId).orElseThrow(NotFoundException::new);
    	EntityAttachment att = media.getAttachments().get(0);

        return Response.ok(att.getData()).header(HttpHeaders.CONTENT_TYPE, att.getContentType()).build();
    }

    private String toAtomXml(Media media) {
        org.w3c.dom.Document xml = DomUtil.createDocument();
        Element entry = DomUtil.createRootElement(xml, "entry"); //$NON-NLS-1$
        entry.setAttribute("xmlns", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$
        populateAtomXml(entry, media);
        return DomUtil.getXMLString(xml);
    }

    @SneakyThrows
    private void populateAtomXml(Element entry, Media media) {
        DomUtil.createElement(entry, "title", media.getName()); //$NON-NLS-1$
        DomUtil.createElement(entry, "id", media.getId()); //$NON-NLS-1$
        DomUtil.createElement(entry, "updated", DateTimeISO8601.formatISO8601(media.getLastModificationDate().getTime())); //$NON-NLS-1$
        Element author = DomUtil.createElement(entry, "author"); //$NON-NLS-1$
        DomUtil.createElement(author, "name", media.getCreationUser()); //$NON-NLS-1$
        Element summary = DomUtil.createElement(entry, "summary"); //$NON-NLS-1$
        summary.setAttribute("type", "text"); //$NON-NLS-1$ //$NON-NLS-2$

        EntityAttachment att = media.getAttachments().get(0);
        Element content = DomUtil.createElement(entry, "content"); //$NON-NLS-1$
        content.setAttribute("type", att.getContentType()); //$NON-NLS-1$

        String nameEnc = URLEncoder.encode(media.getName(), StandardCharsets.UTF_8.name());
        String path = PathUtil.concat(MediaResource.PATH, media.getId(), nameEnc);
        content.setAttribute("src", path); //$NON-NLS-1$

        Element editMediaLink = DomUtil.createElement(entry, "link"); //$NON-NLS-1$
        editMediaLink.setAttribute("edit-media", resolveUrl(AtomPubAPI.BLOG_ID, PATH, media.getId(), nameEnc)); //$NON-NLS-1$

        Element editLink = DomUtil.createElement(entry, "link"); //$NON-NLS-1$
        editLink.setAttribute("rel", "edit"); //$NON-NLS-1$ //$NON-NLS-2$
        editMediaLink.setAttribute("edit-media", resolveUrl(AtomPubAPI.BLOG_ID, PATH, media.getId())); //$NON-NLS-1$
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
