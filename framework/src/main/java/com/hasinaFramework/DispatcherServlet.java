package com.hasinaFramework;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import com.hasinaFramework.annotation.Controller;
import com.hasinaFramework.annotation.RequestParam;
import com.hasinaFramework.annotation.UrlServlet;

import jakarta.servlet.http.HttpServletRequest;


public class DispatcherServlet {

    private final Map<String, Method> urlMapping = new HashMap<>();
    private final Map<Method, Class<?>> methodClassMapping = new HashMap<>();

    public DispatcherServlet(String packageName) throws Exception {
        scanControllers(packageName);
    }

    // transforme /hello/{id} -> /hello/([^/]+)
    private String convertToRegex(String path) {
        return path.replaceAll("\\{[^/]+\\}", "([^/]+)");
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

    public Object invoke(String path, HttpServletRequest request) throws Exception {

        for (Map.Entry<String, Method> entry : urlMapping.entrySet()) {

            String declaredPath = entry.getKey();
            Method method = entry.getValue();

            String regex = convertToRegex(declaredPath);

            // si l’URL correspond
            if (path.matches(regex)) {

                Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];

                String[] pathParts = path.split("/");
                String[] declaredParts = declaredPath.split("/");

                int argIndex = 0;

                for (int i = 0; i < declaredParts.length; i++) {

                    // récupération {id}
                    if (declaredParts[i].startsWith("{")) {
                        String value = pathParts[i];

                        Class<?> type = parameters[argIndex].getType();

                        if (type == int.class || type == Integer.class) {
                            args[argIndex] = Integer.parseInt(value);
                        } else {
                            args[argIndex] = value;
                        }
                        argIndex++;
                    }
                }

                // paramètres ?nom=xxx
                for (int i = 0; i < parameters.length; i++) {
                    if (args[i] == null) {
                        RequestParam rp = parameters[i].getAnnotation(RequestParam.class);
                        if (rp != null) {
                            String paramValue = request.getParameter(rp.value());
                            args[i] = paramValue;
                        }
                    }
                }

                Object instance = methodClassMapping
                        .get(method)
                        .getDeclaredConstructor()
                        .newInstance();

                return method.invoke(instance, args);
            }
        }
        return null;
    }
    

    // public Method getMethod(String path){
    //     return urlMapping.get(path);
    // }

    public Method getMethod(String path) {
        for (Map.Entry<String, Method> entry : urlMapping.entrySet()) {
            String regex = convertToRegex(entry.getKey());
            if (path.matches(regex)) {
                return entry.getValue();
            }
        }
        return null;
    }


    // public Class<?> getClass(String path){
    //     Method method = urlMapping.get(path);
    //     if (method != null) {
    //         return methodClassMapping.get(method);
    //     }
    //     return null;
    // }

    public Class<?> getClass(String path) {
        for (Map.Entry<String, Method> entry : urlMapping.entrySet()) {
            String declaredPath = entry.getKey();
            String regex = convertToRegex(declaredPath);

            if (path.matches(regex)) {
                return methodClassMapping.get(entry.getValue());
            }
        }
        return null;
    }


    // public boolean containsPath(String path) {
    //     return urlMapping.containsKey(path);
    // }

    public boolean containsPath(String path) {
        for (String declaredPath : urlMapping.keySet()) {
            String regex = convertToRegex(declaredPath);
            if (path.matches(regex)) {
                return true;
            }
        }
        return false;
    }

}
