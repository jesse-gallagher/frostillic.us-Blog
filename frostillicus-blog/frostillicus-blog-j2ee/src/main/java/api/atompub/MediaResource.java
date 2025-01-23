/*
 * Copyright (c) 2012-2025 Jesse Gallagher
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
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;

import org.eclipse.jnosql.communication.driver.attachment.EntityAttachment;

import api.atompub.model.Author;
import api.atompub.model.Content;
import api.atompub.model.Entry;
import api.atompub.model.Feed;
import api.atompub.model.Link;
import api.atompub.model.Summary;
import bean.EncoderBean;
import bean.UrlBean;
import bean.UserInfoBean;
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
    
    @Inject
    UserInfoBean userInfo;
    
	@Inject
	UrlBean urlBean;
	
	@Inject
	EncoderBean encoder;

    @GET
    @Produces("application/atom+xml")
    public Feed list() {
    	Feed feed = new Feed();

        mediaRepository.findAll()
        	.map(m -> {
        		Entry entry = new Entry();
                populateAtomXml(entry, m);
        		return entry;
        	})
        	.forEach(feed.getEntries()::add);

        return feed;
    }

    @POST
    @Produces("application/atom+xml")
    public Response uploadMedia(final byte[] data) throws URISyntaxException {
        String contentType = request.getContentType();
        String name = request.getHeader("Slug"); //$NON-NLS-1$

        // TODO make sure it's not already there
        // This could use the name as the UNID, but it's kind of nice having a "real" UNID behind the scenes
        Media media = mediaRepository.findByName(name).orElseGet(Media::new);
        media.setName(name);
        media.setAttachments(Arrays.asList(EntityAttachment.of(name, System.currentTimeMillis(), contentType, data)));
        media.setCreationUser(userInfo.getDn());
        media.setLastModificationDate(new Date());
        media = mediaRepository.save(media);

        // Force update of metadata fields
        media = mediaRepository.findById(media.getId()).get();

        return Response.created(new URI(resolveUrl(AtomPubResource.BLOG_ID, PATH, media.getId())))
        	.entity(toAtomXml(media))
        	.build();
    }

    @GET
    @Path("{mediaId}")
    @Produces("application/atom+xml")
    public Entry getMediaInfo(@PathParam("mediaId") final String mediaId) {
    	Media media = mediaRepository.findById(mediaId).orElseThrow(NotFoundException::new);
        return toAtomXml(media);
    }

    @GET
    @Path("{mediaId}/{name}")
    public Response getMedia(@PathParam("mediaId") final String mediaId) throws IOException {
    	Media media = mediaRepository.findById(mediaId).orElseThrow(NotFoundException::new);
    	EntityAttachment att = media.getAttachments().get(0);

        return Response.ok(att.getData()).header(HttpHeaders.CONTENT_TYPE, att.getContentType()).build();
    }

    private Entry toAtomXml(final Media media) {
    	Entry entry = new Entry();
        populateAtomXml(entry, media);
        return entry;
    }

    private void populateAtomXml(final Entry entry, final Media media) {
    	entry.setTitle(media.getName());
    	entry.setId(media.getId());
    	entry.setUpdated(media.getLastModificationDate().toInstant());
    	entry.setAuthor(new Author(media.getCreationUser()));
    	
    	Summary summary = new Summary();
    	summary.setType("text"); //$NON-NLS-1$
    	summary.setBody(media.getName());
    	entry.setSummary(summary);

        EntityAttachment att = media.getAttachments().get(0);
        Content content = new Content();
        content.setType(att.getContentType());
        entry.setContent(content);

        String nameEnc = encoder.urlEncode(media.getName());
        String path = urlBean.concat(MediaResource.PATH, media.getId(), nameEnc);
        content.setSrc(path);
        entry.setContent(content);

        Link editMediaLink = new Link();
        editMediaLink.setEditMedia(resolveUrl(AtomPubResource.BLOG_ID, PATH, media.getId(), nameEnc));
        entry.getLinks().add(editMediaLink);

        Link editLink = new Link();
        editLink.setRel("link"); //$NON-NLS-1$
        editLink.setEditMedia(resolveUrl(AtomPubResource.BLOG_ID, PATH, media.getId()));
        entry.getLinks().add(editLink);
    }

    private String resolveUrl(final String... parts) {
        URI baseUri = uriInfo.getBaseUri();
        String uri = urlBean.concat(baseUri.toString(), AtomPubResource.BASE_PATH);
        for(String part : parts) {
            uri = urlBean.concat(uri, part);
        }
        return uri;
    }
}
