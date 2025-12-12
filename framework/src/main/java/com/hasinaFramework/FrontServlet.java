package com.hasinaFramework;

import java.io.IOException;
import java.lang.reflect.Method;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/")
public class FrontServlet extends HttpServlet{
    private DispatcherServlet dispatcherServlet;

    @Override
    public void init() throws ServletException {
        try {
            dispatcherServlet = new DispatcherServlet("com.hasinaFramework");
        } catch (Exception e) {
            throw new ServletException("Failed to initialize DispatcherServlet", e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
        throws IOException, ServletException {
        
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        Class<?> cls = dispatcherServlet.getClass(path);
        Method method = dispatcherServlet.getMethod(path);

        try {
            if (dispatcherServlet.containsPath(path)) {
                Object result = dispatcherServlet.invoke(path);
                
                if (result instanceof ModelVue) {
                    ModelVue mv = (ModelVue) result;
                    mv.getData().forEach(req::setAttribute);
                    req.getRequestDispatcher(mv.getVue())
                    .forward(req, res);
                    return;
                }else{
                    res.getWriter().write(
                        " class: " + cls.getSimpleName() +
                        " method: " + method.getName() +
                        " value: " + result.toString()
                    );
                }
            } else {
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "No handler found for " + path);
            }
        } catch (Exception e) {
            throw new ServletException("Error processing request for " + path, e);
        }

    }
}
