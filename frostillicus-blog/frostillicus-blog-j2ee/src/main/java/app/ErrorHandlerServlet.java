package app;

import com.darwino.commons.util.io.StreamUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static javax.servlet.RequestDispatcher.*;

@WebServlet(urlPatterns="/errorHandler")
public class ErrorHandlerServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=utf-8");
        try (PrintWriter writer = resp.getWriter()) {
            String bodyHtml;
            try(InputStream is = getClass().getResourceAsStream("/WEB-INF/error.html")) {
                bodyHtml = StreamUtil.readString(is);
            }

            bodyHtml = bodyHtml.replace("${CONTEXT_PATH}", req.getContextPath())
                .replace("${ERROR_MESSAGE}", String.valueOf(req.getAttribute(ERROR_MESSAGE)))
                .replace("${ERROR_STATUS_CODE}", String.valueOf(req.getAttribute(ERROR_STATUS_CODE)))
                .replace("${ERROR_EXCEPTION_TYPE}", String.valueOf(req.getAttribute(ERROR_EXCEPTION_TYPE)));

            Throwable t = (Throwable)req.getAttribute(ERROR_EXCEPTION);
            if(t != null) {
                try(StringWriter w = new StringWriter()) {
                    try(PrintWriter p = new PrintWriter(w)) {
                        t.printStackTrace(p);
                    }
                    bodyHtml = bodyHtml.replace("${ERROR_STACK_TRACE}", w.toString());
                }
            } else {
                bodyHtml = bodyHtml.replace("${ERROR_STACK_TRACE}", "");
            }

            writer.write(bodyHtml);
        }
    }
}
