package rest;

import bean.UserPhotoBean;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@Path("userPhoto")
public class UserPhotoResource {
	
	@Inject
	private UserPhotoBean photoBean;
	
	@Path("{hash}")
	@GET
	@Produces("image/png")
	public byte[] get(@PathParam("hash") String hash) {
		// TODO provide cache headers
		return photoBean.getThumbnailData(hash);
	}
}