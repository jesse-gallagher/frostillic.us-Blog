/*
 * Copyright © 2012-2020 Jesse Gallagher
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ResourceBundle;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.jnosql.communication.driver.attachment.EntityAttachment;
import org.w3c.dom.Element;

import com.darwino.commons.util.DateTimeISO8601;
import com.darwino.commons.util.PathUtil;
import com.darwino.commons.xml.DomUtil;

import bean.UserInfoBean;
import lombok.SneakyThrows;
import model.Media;
import model.MediaRepository;

@Path(AtomPubResource.BASE_PATH + "/{blogId}/" + MediaResource.PATH)
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
        var xml = DomUtil.createDocument();
        var feed = DomUtil.createRootElement(xml, "feed"); //$NON-NLS-1$
        feed.setAttribute("xmlns", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$

        mediaRepository.findAll().forEach(m -> {
        	var entry = DomUtil.createElement(feed, "entry"); //$NON-NLS-1$
            entry.setAttribute("xmlns", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$
            populateAtomXml(entry, m);
        });

        return DomUtil.getXMLString(xml);
    }

    @POST
    @Produces("application/atom+xml")
    public Response uploadMedia(final byte[] data) throws URISyntaxException {
        var contentType = request.getContentType();
        var name = request.getHeader("Slug"); //$NON-NLS-1$

        // TODO make sure it's not already there
        // This could use the name as the UNID, but it's kind of nice having a "real" UNID behind the scenes
        var media = mediaRepository.findByName(name).orElseGet(Media::new);
        media.setName(name);
        media.setAttachments(Arrays.asList(EntityAttachment.of(name, System.currentTimeMillis(), contentType, data)));
        media = mediaRepository.save(media);

        // Force update of metadata fields
        media = mediaRepository.findById(media.getId()).get();

        return Response.created(new URI(resolveUrl(AtomPubResource.BLOG_ID, PATH, media.getId()))).entity(toAtomXml(media)).build();
    }

    @GET
    @Path("{mediaId}")
    @Produces("application/atom+xml")
    public Response getMediaInfo(@PathParam("mediaId") final String mediaId) {
    	var media = mediaRepository.findById(mediaId).orElseThrow(NotFoundException::new);
        return Response.ok(toAtomXml(media)).build();
    }

    @GET
    @Path("{mediaId}/{name}")
    public Response getMedia(@PathParam("mediaId") final String mediaId) throws IOException {
    	var media = mediaRepository.findById(mediaId).orElseThrow(NotFoundException::new);
    	var att = media.getAttachments().get(0);

        return Response.ok(att.getData()).header(HttpHeaders.CONTENT_TYPE, att.getContentType()).build();
    }

    private String toAtomXml(final Media media) {
        var xml = DomUtil.createDocument();
        var entry = DomUtil.createRootElement(xml, "entry"); //$NON-NLS-1$
        entry.setAttribute("xmlns", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$
        populateAtomXml(entry, media);
        return DomUtil.getXMLString(xml);
    }

    @SneakyThrows
    private void populateAtomXml(final Element entry, final Media media) {
        DomUtil.createElement(entry, "title", media.getName()); //$NON-NLS-1$
        DomUtil.createElement(entry, "id", media.getId()); //$NON-NLS-1$
        DomUtil.createElement(entry, "updated", DateTimeISO8601.formatISO8601(media.getLastModificationDate().getTime())); //$NON-NLS-1$
        var author = DomUtil.createElement(entry, "author"); //$NON-NLS-1$
        DomUtil.createElement(author, "name", media.getCreationUser()); //$NON-NLS-1$
        var summary = DomUtil.createElement(entry, "summary"); //$NON-NLS-1$
        summary.setAttribute("type", "text"); //$NON-NLS-1$ //$NON-NLS-2$

        var att = media.getAttachments().get(0);
        var content = DomUtil.createElement(entry, "content"); //$NON-NLS-1$
        content.setAttribute("type", att.getContentType()); //$NON-NLS-1$

        var nameEnc = URLEncoder.encode(media.getName(), StandardCharsets.UTF_8.name());
        var path = PathUtil.concat(MediaResource.PATH, media.getId(), nameEnc);
        content.setAttribute("src", path); //$NON-NLS-1$

        var editMediaLink = DomUtil.createElement(entry, "link"); //$NON-NLS-1$
        editMediaLink.setAttribute("edit-media", resolveUrl(AtomPubResource.BLOG_ID, PATH, media.getId(), nameEnc)); //$NON-NLS-1$

        var editLink = DomUtil.createElement(entry, "link"); //$NON-NLS-1$
        editLink.setAttribute("rel", "edit"); //$NON-NLS-1$ //$NON-NLS-2$
        editMediaLink.setAttribute("edit-media", resolveUrl(AtomPubResource.BLOG_ID, PATH, media.getId())); //$NON-NLS-1$
    }

    private String resolveUrl(final String... parts) {
        var baseUri = uriInfo.getBaseUri();
        var uri = PathUtil.concat(baseUri.toString(), AtomPubResource.BASE_PATH);
        for(String part : parts) {
            uri = PathUtil.concat(uri, part);
        }
        return uri;
    }
}
