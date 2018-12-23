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
