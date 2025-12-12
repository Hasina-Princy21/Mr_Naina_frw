package com.hasinaFramework;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;

import jakarta.servlet.annotation.WebServlet;

@WebServlet("/")
public class DispatcherServlet {

    private final Map<String, Method> urlMapping = new HashMap<>();
    private final Map<Method, Class<?>> methodClassMapping = new HashMap<>();

    public DispatcherServlet(String packageName) throws Exception {
        scanControllers(packageName);
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

    public Object invoke(String path) throws Exception {
        Method method = urlMapping.get(path);
        if (method != null) {
            Object instance = methodClassMapping.get(method).getDeclaredConstructor().newInstance();
            return method.invoke(instance);
        }
        return null;
    }

    public Method getMethod(String path){
        return urlMapping.get(path);
    }

    public Class<?> getClass(String path){
        Method method = urlMapping.get(path);
        if (method != null) {
            return methodClassMapping.get(method);
        }
        return null;
    }

    public boolean containsPath(String path) {
        return urlMapping.containsKey(path);
    }
}
