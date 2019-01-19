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
package app;

import com.darwino.commons.json.JsonException;
import com.darwino.jsonstore.Attachment;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Document;
import com.darwino.jsonstore.Store;
import darwino.AppDatabaseDef;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.*;

@Path(MediaResource.PATH)
public class MediaResource {
    public static final String PATH = "media";

    @Inject
    Database database;

    @Context
    Request request;

    @GET
    @Path("{mediaId}/{mediaName}")
    public Response get(@PathParam("mediaId") String mediaId) throws JsonException {
        Store store = database.getStore(AppDatabaseDef.STORE_MEDIA);
        Document doc = store.loadDocument(mediaId);

        Attachment att = doc.getAttachments()[0];

        EntityTag etag = new EntityTag(att.getETag());
        Response.ResponseBuilder builder = request.evaluatePreconditions(etag);
        if(builder == null) {
            builder = Response.ok(att.getInputStream())
                    .header(HttpHeaders.CONTENT_TYPE, att.getMimeType())
                    .header(HttpHeaders.ETAG, etag);
        }

        CacheControl cc = new CacheControl();
        cc.setMaxAge(5 * 24 * 60 * 60);

        return builder
                .cacheControl(cc)
                .build();
    }
}
