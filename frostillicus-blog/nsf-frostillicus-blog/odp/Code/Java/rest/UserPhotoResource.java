package rest;

import bean.UserPhotoBean;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Response;

@Path("userPhoto")
public class UserPhotoResource {

	@Inject
	private UserPhotoBean photoBean;

	@Path("{hash}")
	@GET
	@Produces("image/png")
	public Response get(@PathParam("hash") String hash) {
		var cache = new CacheControl();
		cache.setNoTransform(true);
		cache.setMaxAge(432000);
		
		return Response.ok(photoBean.getThumbnailData(hash))
			.cacheControl(cache)
			.build();
	}
}