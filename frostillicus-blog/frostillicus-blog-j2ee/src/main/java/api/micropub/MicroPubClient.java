package api.micropub;

import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.HttpHeaders;

/**
 * Represents the MicroPub REST API.
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 * @see <a href="https://www.w3.org/TR/micropub/">https://www.w3.org/TR/micropub/</a>
 */
public interface MicroPubClient {
	public enum EntryType {
		entry
	}
	
	@POST
	void create(
		@HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
		@FormParam("h") EntryType entryType,
		@FormParam("name") String name,
		@FormParam("content") String content
	);
}
