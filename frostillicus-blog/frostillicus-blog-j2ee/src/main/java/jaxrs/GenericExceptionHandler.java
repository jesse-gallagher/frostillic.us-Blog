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
package jaxrs;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.inject.Inject;
import javax.mvc.Models;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Jesse Gallagher
 */
@Provider
public class GenericExceptionHandler implements ExceptionMapper<Throwable> {
	@Context
	private HttpServletRequest req;
	
	@Inject
	private Models models;
	
	@Override
	public Response toResponse(Throwable t) {
		Status status;
		if(t instanceof NotFoundException) {
			status = Status.NOT_FOUND;
		} else {
			status = Status.INTERNAL_SERVER_ERROR;
		}
		
		models.put("ERROR_MESSAGE", t.getLocalizedMessage()); //$NON-NLS-1$
		models.put("CONTEXT_PATH", req.getContextPath()); //$NON-NLS-1$
		models.put("ERROR_STATUS_CODE", status); //$NON-NLS-1$
        try(StringWriter w = new StringWriter()) {
            try(PrintWriter p = new PrintWriter(w)) {
                t.printStackTrace(p);
            }
            models.put("ERROR_STACK_TRACE", w.toString()); //$NON-NLS-1$
        } catch(IOException e) {
        		// Ignore
        }
		
        // TODO customize for non-HTML requests
		return Response.status(status)
			.entity("error.jsp") //$NON-NLS-1$
			.build();
	}

}
