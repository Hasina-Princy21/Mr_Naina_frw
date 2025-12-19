package com.hasinaFramework;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/")
public class FrontServlet extends HttpServlet {

    private DispatcherServlet dispatcherServlet;

    @Override
    public void init() throws ServletException {
        try {
            dispatcherServlet = new DispatcherServlet("com.hasinaFramework.controller");
        } catch (Exception e) {
            throw new ServletException("Failed to initialize DispatcherServlet", e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        try {
            Object result = dispatcherServlet.invoke(path, req);

            if (result == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "No handler found for " + path);
                return;
            }

            if (result instanceof ModelVue) {
                ModelVue mv = (ModelVue) result;
                mv.getData().forEach(req::setAttribute);
                req.getRequestDispatcher(mv.getVue())
                        .forward(req, res);
            } else {
                res.setContentType("text/html;charset=UTF-8");
                res.getWriter().write(
                    result.toString()
                );
            }

        } catch (Exception e) {
            throw new ServletException(
                    "Error processing request for " + path,
                    e
            );
        }
    }
}
