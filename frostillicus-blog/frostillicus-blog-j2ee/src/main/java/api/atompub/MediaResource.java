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

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.w3c.dom.Element;


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
       return null;
    }

    @POST
    @Produces("application/atom+xml")
    public Response uploadMedia(final byte[] data) throws URISyntaxException {
       return null;
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
        return null;
    }

    private String toAtomXml(final Media media) {
        return null;
    }

    @SneakyThrows
    private void populateAtomXml(final Element entry, final Media media) {
//        DomUtil.createElement(entry, "title", media.getName()); //$NON-NLS-1$
//        DomUtil.createElement(entry, "id", media.getId()); //$NON-NLS-1$
//        DomUtil.createElement(entry, "updated", DateTimeISO8601.formatISO8601(media.getLastModificationDate().getTime())); //$NON-NLS-1$
//        var author = DomUtil.createElement(entry, "author"); //$NON-NLS-1$
//        DomUtil.createElement(author, "name", media.getCreationUser()); //$NON-NLS-1$
//        var summary = DomUtil.createElement(entry, "summary"); //$NON-NLS-1$
//        summary.setAttribute("type", "text"); //$NON-NLS-1$ //$NON-NLS-2$
//
//        var att = media.getAttachments().get(0);
//        var content = DomUtil.createElement(entry, "content"); //$NON-NLS-1$
//        content.setAttribute("type", att.getContentType()); //$NON-NLS-1$
//
//        var nameEnc = URLEncoder.encode(media.getName(), StandardCharsets.UTF_8.name());
//        var path = PathUtil.concat(MediaResource.PATH, media.getId(), nameEnc);
//        content.setAttribute("src", path); //$NON-NLS-1$
//
//        var editMediaLink = DomUtil.createElement(entry, "link"); //$NON-NLS-1$
//        editMediaLink.setAttribute("edit-media", resolveUrl(AtomPubResource.BLOG_ID, PATH, media.getId(), nameEnc)); //$NON-NLS-1$
//
//        var editLink = DomUtil.createElement(entry, "link"); //$NON-NLS-1$
//        editLink.setAttribute("rel", "edit"); //$NON-NLS-1$ //$NON-NLS-2$
//        editMediaLink.setAttribute("edit-media", resolveUrl(AtomPubResource.BLOG_ID, PATH, media.getId())); //$NON-NLS-1$
    }

    private String resolveUrl(final String... parts) {

        return null;
    }
}
