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
