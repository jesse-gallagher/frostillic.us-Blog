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
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.inject.Inject;
import javax.mvc.Controller;
import javax.mvc.Models;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.krazo.engine.Viewable;

import com.darwino.commons.util.io.StreamUtil;

/**
 * @author Jesse Gallagher
 */
@Provider
public class GenericExceptionHandler implements ExceptionMapper<Throwable> {
	@Context
	private HttpServletRequest req;
	
	@Context
	private ResourceInfo resourceInfo;
	
	@Inject
	private Models models;
	
	@Override
	public Response toResponse(Throwable t) {
		// If we're in an MVC context, send an MVC-style response
		if(isMvcRequest()) {
			return mvcResponse(t);
		} else {
			return servletResponse(t);
		}
	}
	
	private Response mvcResponse(Throwable t) {
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
        		.type(MediaType.TEXT_HTML)
        		.encoding("UTF-8") //$NON-NLS-1$
			.entity(new Viewable("error.jsp")) //$NON-NLS-1$
			.build();
	}
	
	private Response servletResponse(Throwable t) {
		Status status;
		if(t instanceof NotFoundException) {
			status = Status.NOT_FOUND;
		} else {
			status = Status.INTERNAL_SERVER_ERROR;
		}
		
        String bodyHtml;
        try(InputStream is = getClass().getResourceAsStream("/WEB-INF/error.html")) { //$NON-NLS-1$
            bodyHtml = StreamUtil.readString(is);
        } catch(IOException e) {
        		e.printStackTrace();
        		bodyHtml = "";
        }

        bodyHtml = bodyHtml.replace("${CONTEXT_PATH}", req.getContextPath()) //$NON-NLS-1$
            .replace("${ERROR_MESSAGE}", t.getLocalizedMessage()) //$NON-NLS-1$
            .replace("${ERROR_STATUS_CODE}", String.valueOf(status.getStatusCode())) //$NON-NLS-1$
            .replace("${ERROR_EXCEPTION_TYPE}", String.valueOf(t.getClass().getName())); //$NON-NLS-1$

        try(StringWriter w = new StringWriter()) {
            try(PrintWriter p = new PrintWriter(w)) {
                t.printStackTrace(p);
            }
            bodyHtml = bodyHtml.replace("${ERROR_STACK_TRACE}", w.toString()); //$NON-NLS-1$
        } catch(IOException e) {
        		e.printStackTrace();
        }
        
        return Response.status(status)
        		.type(MediaType.TEXT_HTML)
        		.encoding("UTF-8") //$NON-NLS-1$
        		.entity(bodyHtml)
        		.build();
	}

	private boolean isMvcRequest() {
		if(resourceInfo == null) {
			return false;
		}
		if(resourceInfo.getResourceClass() != null && resourceInfo.getResourceClass().isAnnotationPresent(Controller.class)) {
			return true;
		}
		if(resourceInfo.getResourceMethod() != null && resourceInfo.getResourceMethod().isAnnotationPresent(Controller.class)) {
			return true;
		}
		return false;
	}
}
