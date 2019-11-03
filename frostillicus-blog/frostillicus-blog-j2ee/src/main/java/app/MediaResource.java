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
package app;

import model.Media;
import model.MediaRepository;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.*;

import org.eclipse.jnosql.diana.driver.attachment.EntityAttachment;

@Path(MediaResource.PATH)
public class MediaResource {
    public static final String PATH = "media"; //$NON-NLS-1$

    @Context
    Request request;
    
    @Inject
    MediaRepository mediaRepository;

    @GET
    @Path("{mediaId}/{mediaName}")
    public Response get(@PathParam("mediaId") String mediaId) throws IOException {
    	Media media = mediaRepository.findById(mediaId).orElseThrow(NotFoundException::new);
        EntityAttachment att = media.getAttachments().get(0);

        EntityTag etag = new EntityTag(att.getETag());
        Response.ResponseBuilder builder = request.evaluatePreconditions(etag);
        if(builder == null) {
            builder = Response.ok(att.getData())
                    .header(HttpHeaders.CONTENT_TYPE, att.getContentType())
                    .header(HttpHeaders.ETAG, etag);
        }

        CacheControl cc = new CacheControl();
        cc.setMaxAge(5 * 24 * 60 * 60);

        return builder
                .cacheControl(cc)
                .build();
    }
}
