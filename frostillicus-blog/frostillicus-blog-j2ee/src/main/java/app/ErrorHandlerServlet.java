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

import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION_TYPE;
import static javax.servlet.RequestDispatcher.ERROR_MESSAGE;
import static javax.servlet.RequestDispatcher.ERROR_STATUS_CODE;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.darwino.commons.util.io.StreamUtil;

@WebServlet(name="ErrorHandler", urlPatterns="/errorHandler")
public class ErrorHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html; charset=utf-8"); //$NON-NLS-1$
        try (var writer = resp.getWriter()) {
            String bodyHtml;
            try(var is = getClass().getResourceAsStream("/WEB-INF/error.html")) { //$NON-NLS-1$
                bodyHtml = StreamUtil.readString(is);
            }

            bodyHtml = bodyHtml.replace("${CONTEXT_PATH}", req.getContextPath()) //$NON-NLS-1$
                .replace("${ERROR_MESSAGE}", String.valueOf(req.getAttribute(ERROR_MESSAGE))) //$NON-NLS-1$
                .replace("${ERROR_STATUS_CODE}", String.valueOf(req.getAttribute(ERROR_STATUS_CODE))) //$NON-NLS-1$
                .replace("${ERROR_EXCEPTION_TYPE}", String.valueOf(req.getAttribute(ERROR_EXCEPTION_TYPE))); //$NON-NLS-1$

            var t = (Throwable)req.getAttribute(ERROR_EXCEPTION);
            if(t != null) {
                try(var w = new StringWriter()) {
                    try(var p = new PrintWriter(w)) {
                        t.printStackTrace(p);
                    }
                    bodyHtml = bodyHtml.replace("${ERROR_STACK_TRACE}", w.toString()); //$NON-NLS-1$
                }
            } else {
                bodyHtml = bodyHtml.replace("${ERROR_STACK_TRACE}", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }

            writer.write(bodyHtml);
        }
	}
}
