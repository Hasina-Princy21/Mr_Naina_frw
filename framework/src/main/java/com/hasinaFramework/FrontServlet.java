package com.hasinaFramework;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServlet extends HttpServlet {

    private DispatcherServlet dispatcherServlet;
    private String sessionAttribute; // Nom de l'attribut de session à vérifier pour @Authorized
    private String roleAttribute;

    @Override
    public void init() throws ServletException {
        try {
            // Récupérer le nom de l'attribut de session depuis web.xml
            sessionAttribute = getInitParameter("sessionAttribute");
            if (sessionAttribute == null || sessionAttribute.trim().isEmpty()) {
                sessionAttribute = "user"; // Valeur par défaut
            }
            roleAttribute = getInitParameter("roleAttribute");
            if (roleAttribute == null || roleAttribute.trim().isEmpty()) {
                roleAttribute = "role"; // Valeur par défaut
            }
            dispatcherServlet = new DispatcherServlet("com.hasinaFramework.controller", 
                                               sessionAttribute, roleAttribute);
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
