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
        Method method = urlMapping.get(path);
        if (method != null) {
            Parameter[] parameters = method.getParameters(); 
            Object[] arg = new Object[parameters.length];

            for (int i = 0; i < arg.length; i++) {
                Parameter p = parameters[i];
                RequestParam rp = p.getAnnotation(RequestParam.class);
                String paramName = rp != null ? rp.value() : p.getName(); 
                String res = request.getParameter(paramName);
                if (parameters[i].getType() == String.class) {
                    arg[i]=(String) res;
                }
                
            }
            Object instance = methodClassMapping.get(method).getDeclaredConstructor().newInstance();
            return method.invoke(instance, arg);
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
