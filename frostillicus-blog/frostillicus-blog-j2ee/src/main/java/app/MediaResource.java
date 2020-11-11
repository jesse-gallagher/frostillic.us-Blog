/**
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
package app;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import model.MediaRepository;

@Path(MediaResource.PATH)
public class MediaResource {
    public static final String PATH = "media"; //$NON-NLS-1$

    @Context
    Request request;

    @Inject
    MediaRepository mediaRepository;

    @GET
    @Path("{mediaId}/{mediaName}")
    public Response get(@PathParam("mediaId") final String mediaId) throws IOException {
    	var media = mediaRepository.findById(mediaId).orElseThrow(NotFoundException::new);
        var att = media.getAttachments().get(0);

        var etag = new EntityTag(att.getETag());
        var builder = request.evaluatePreconditions(etag);
        if(builder == null) {
            builder = Response.ok(att.getData())
                    .header(HttpHeaders.CONTENT_TYPE, att.getContentType())
                    .header(HttpHeaders.ETAG, etag);
        }

        var cc = new CacheControl();
        cc.setMaxAge(5 * 24 * 60 * 60);

        return builder
                .cacheControl(cc)
                .build();
    }
}
