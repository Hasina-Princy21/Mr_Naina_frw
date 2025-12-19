package com.hasinaFramework;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import com.hasinaFramework.annotation.Controller;
import com.hasinaFramework.annotation.GetMapping;
import com.hasinaFramework.annotation.PostMapping;
import com.hasinaFramework.annotation.RequestParam;
// import com.hasinaFramework.annotation.UrlServlet;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Enumeration;


public class DispatcherServlet {

    private final Map<String, Method> urlMapping = new HashMap<>();
    private final Map<Method, Class<?>> methodClassMapping = new HashMap<>();
    private final Map<String, Map<String, Method>> routes = new HashMap<>();

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

                // GET
                if (method.isAnnotationPresent(GetMapping.class)) {
                    String path = method
                            .getAnnotation(GetMapping.class)
                            .value();

                    routes
                        .computeIfAbsent(path, k -> new HashMap<>())
                        .put("GET", method);

                    methodClassMapping.put(method, cls);
                }

                // POST
                if (method.isAnnotationPresent(PostMapping.class)) {
                    String path = method
                            .getAnnotation(PostMapping.class)
                            .value();

                    routes
                        .computeIfAbsent(path, k -> new HashMap<>())
                        .put("POST", method);

                    methodClassMapping.put(method, cls);
                }
            }
        }
    }


    public Object invoke(String path, HttpServletRequest request) throws Exception {

        String httpMethod = request.getMethod();
        Method method = findMethod(path, httpMethod);

        if (method == null) {
            return null; 
        }

        // Vérifier si la méthode prend un Map<String, String>
        if (method.getParameterCount() == 1 && method.getParameters()[0].getType() == Map.class) {
            Map<String, String> params = new HashMap<>();

            // Collecter les paramètres de chemin
            String declaredPath = null;
            for (String p : routes.keySet()) {
                if (path.matches(convertToRegex(p))) {
                    declaredPath = p;
                    break;
                }
            }

            if (declaredPath != null) {
                String[] pathParts = path.split("/");
                String[] declaredParts = declaredPath.split("/");

                for (int i = 0; i < declaredParts.length && i < pathParts.length; i++) {
                    if (declaredParts[i].startsWith("{")) {
                        String name = declaredParts[i].substring(1, declaredParts[i].length() - 1);
                        params.put(name, pathParts[i]);
                    }
                }
            }

            // Collecter tous les paramètres de requête
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement();
                String value = request.getParameter(name);
                params.put(name, value);
            }

            // Instancier et invoquer la méthode avec la map
            Object instance = methodClassMapping.get(method).getDeclaredConstructor().newInstance();
            return method.invoke(instance, params);
        } else if (method.getParameterCount() == 1) {
            // Support pour binding d'objet (ex. Etudiant)
            Class<?> clazz = method.getParameters()[0].getType();
            Object beanInstance = clazz.getDeclaredConstructor().newInstance();

            // Collecter les paramètres de chemin
            String declaredPath = null;
            for (String p : routes.keySet()) {
                if (path.matches(convertToRegex(p))) {
                    declaredPath = p;
                    break;
                }
            }

            if (declaredPath != null) {
                String[] pathParts = path.split("/");
                String[] declaredParts = declaredPath.split("/");

                for (int i = 0; i < declaredParts.length && i < pathParts.length; i++) {
                    if (declaredParts[i].startsWith("{")) {
                        String name = declaredParts[i].substring(1, declaredParts[i].length() - 1);
                        setField(beanInstance, clazz, name, pathParts[i]);
                    }
                }
            }

            // Collecter et binder les paramètres de requête
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement();
                String value = request.getParameter(name);
                // Supporter les noms avec préfixe comme "etudiant.nom" -> "nom"
                String fieldName = name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : name;
                setField(beanInstance, clazz, fieldName, value);
            }

            // Instancier le contrôleur et invoquer la méthode avec l'objet
            Object controllerInstance = methodClassMapping.get(method).getDeclaredConstructor().newInstance();
            return method.invoke(controllerInstance, beanInstance);
        } else {
            // Code existant pour les méthodes avec paramètres individuels
            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];

            String declaredPath = null;

            for (String p : routes.keySet()) {
                if (path.matches(convertToRegex(p))) {
                    declaredPath = p;
                    break;
                }
            }

            String[] pathParts = path.split("/");
            String[] declaredParts = declaredPath.split("/");

            int argIndex = 0;

            for (int i = 0; i < declaredParts.length; i++) {
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

            for (int i = 0; i < parameters.length; i++) {
                if (args[i] == null) {
                    RequestParam rp = parameters[i].getAnnotation(RequestParam.class);
                    if (rp != null) {
                        String val = request.getParameter(rp.value());

                        if (parameters[i].getType() == int.class || parameters[i].getType() == Integer.class) {
                            args[i] = Integer.parseInt(val);
                        } else {
                            args[i] = val;
                        }
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
        for (String declaredPath : routes.keySet()) {
            String regex = convertToRegex(declaredPath);
            if (path.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    private void setField(Object instance, Class<?> clazz, String fieldName, String value) {
        try {
            String setterName = "set" + capitalize(fieldName);
            Method setter = clazz.getMethod(setterName, String.class);
            setter.invoke(instance, value);
        } catch (Exception e) {
            // Ignorer si pas de setter ou erreur
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private Method findMethod(String path, String httpMethod) {
        for (Map.Entry<String, Map<String, Method>> entry : routes.entrySet()) {
            String declaredPath = entry.getKey();
            String regex = convertToRegex(declaredPath);
            if (path.matches(regex)) {
                return entry.getValue().get(httpMethod);
            }
        }
        return null;
    }

}
