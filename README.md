# Mr_Naina_frw

package com.hasinaFramework;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;

public class FrontServlet extends HttpServlet {

    private final Map<String, Method> urlMapping = new HashMap<>();
    private final Map<Method, Class<?>> methodClassMapping = new HashMap<>();

    @Override
    public void init() throws ServletException {
        try {
            scanControllers("com.hasinaFramework");
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des controllers", e);
        }
    }

    private void scanControllers(String packageName) throws Exception {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);

        for (Class<?> cls : controllers) {
            for (Method method : cls.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlServlet.class)) {
                    String path = method.getAnnotation(UrlServlet.class).value();
                    urlMapping.put(path, method);
                    methodClassMapping.put(method, cls);
                }
            }
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        res.setContentType("text/html");

        Method method = urlMapping.get(path);
        if (method != null) {
            try {
                Object instance = methodClassMapping.get(method).getDeclaredConstructor().newInstance();
                Object result = method.invoke(instance);
                res.getWriter().write(result.toString());
            } catch (Exception e) {
                res.getWriter().write("Erreur lors de l'exécution de la méthode: " + e.getMessage());
            }
        } else {
            res.getWriter().write("404 Not Found : " + path);
        }
    }
}
