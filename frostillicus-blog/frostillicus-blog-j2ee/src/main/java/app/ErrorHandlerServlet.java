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
package app;

import com.darwino.commons.util.io.StreamUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static javax.servlet.RequestDispatcher.*;

@WebServlet(name="ErrorHandler", urlPatterns="/errorHandler")
public class ErrorHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=utf-8"); //$NON-NLS-1$
        try (PrintWriter writer = resp.getWriter()) {
            String bodyHtml;
            try(InputStream is = getClass().getResourceAsStream("/WEB-INF/error.html")) { //$NON-NLS-1$
                bodyHtml = StreamUtil.readString(is);
            }

            bodyHtml = bodyHtml.replace("${CONTEXT_PATH}", req.getContextPath()) //$NON-NLS-1$
                .replace("${ERROR_MESSAGE}", String.valueOf(req.getAttribute(ERROR_MESSAGE))) //$NON-NLS-1$
                .replace("${ERROR_STATUS_CODE}", String.valueOf(req.getAttribute(ERROR_STATUS_CODE))) //$NON-NLS-1$
                .replace("${ERROR_EXCEPTION_TYPE}", String.valueOf(req.getAttribute(ERROR_EXCEPTION_TYPE))); //$NON-NLS-1$

            Throwable t = (Throwable)req.getAttribute(ERROR_EXCEPTION);
            if(t != null) {
                try(StringWriter w = new StringWriter()) {
                    try(PrintWriter p = new PrintWriter(w)) {
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
