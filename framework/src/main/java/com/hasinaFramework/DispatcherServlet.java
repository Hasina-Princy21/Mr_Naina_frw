package com.hasinaFramework;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import com.hasinaFramework.annotation.Controller;
// import com.hasinaFramework.annotation.FileParam;
import com.hasinaFramework.annotation.GetMapping;
import com.hasinaFramework.annotation.PostMapping;
import com.hasinaFramework.annotation.RequestParam;
import com.hasinaFramework.util.UploadedFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import com.hasinaFramework.annotation.JsonResponse;

import com.google.gson.Gson;


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
        // Diagnostic: lister les parts si la requête est multipart
        try {
            String ct = request.getContentType();
            if (ct != null && ct.toLowerCase().contains("multipart")) {
                try {
                    for (Part p : request.getParts()) {
                        String sfn = p.getSubmittedFileName();
                        System.out.println("[DEBUG multipart] part name=" + p.getName() +
                                           ", submittedFileName=" + sfn +
                                           ", size=" + p.getSize() +
                                           ", contentType=" + p.getContentType());
                    }
                } catch (Exception e) {
                    System.err.println("[DEBUG multipart] impossible de lister les parts: " + e.getMessage());
                }
            }
        } catch (Throwable t) {
            // Ne pas empêcher le traitement en cas d'erreur de debug
        }
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
            Object result = method.invoke(instance, params);
            if (method.isAnnotationPresent(JsonResponse.class)) {
                Gson gson = new Gson();
                return gson.toJson(result);
            } else {
                return result;
            }
        } else if (method.getParameterCount() == 1) {
            // Special-case: if the single parameter is an UploadedFile (or annotated with @RequestParam),
            // bind it from the multipart Part instead of doing bean-binding.
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Class<?> clazz = param.getType();

            if (clazz == UploadedFile.class || param.isAnnotationPresent(RequestParam.class)) {
                Object[] args = new Object[1];
                RequestParam rp = param.getAnnotation(RequestParam.class);
                if (clazz == UploadedFile.class && rp != null) {
                    try {
                        Part filePart = request.getPart(rp.value());
                        if (filePart != null) {
                            String submittedName = filePart.getSubmittedFileName();
                            if (submittedName != null && !submittedName.trim().isEmpty()) {
                                UploadedFile uploadedFile = new UploadedFile(
                                    submittedName,
                                    filePart.getContentType(),
                                    filePart.getSize(),
                                    filePart.getInputStream()
                                );
                                args[0] = uploadedFile;
                            } else {
                                System.out.println("Fichier présent mais nom vide pour param: " + rp.value());
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors de l'extraction du fichier " + (rp != null ? rp.value() : "?") + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                } else if (rp != null) {
                    // Single non-upload parameter annotated with @RequestParam -> bind primitive/string
                    String val = request.getParameter(rp.value());
                    if (clazz == int.class || clazz == Integer.class) {
                        args[0] = Integer.parseInt(val);
                    } else {
                        args[0] = val;
                    }
                }

                Object controllerInstance = methodClassMapping.get(method).getDeclaredConstructor().newInstance();
                Object result = method.invoke(controllerInstance, args);
                if (method.isAnnotationPresent(JsonResponse.class)) {
                    Gson gson = new Gson();
                    return gson.toJson(result);
                } else {
                    return result;
                }
            }

            // Otherwise fall back to bean-binding for a single complex object (e.g., Etudiant)
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
            Object result = method.invoke(controllerInstance, beanInstance);
            if (method.isAnnotationPresent(JsonResponse.class)) {
                Gson gson = new Gson();
                return gson.toJson(result);
            } else {
                return result;
            }
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
                        // If parameter expects an UploadedFile, skip request.getParameter binding
                        if (parameters[i].getType() == UploadedFile.class) {
                            // attempt to bind file below
                        } else {
                            String val = request.getParameter(rp.value());

                            if (parameters[i].getType() == int.class || parameters[i].getType() == Integer.class) {
                                args[i] = Integer.parseInt(val);
                            } else {
                                args[i] = val;
                            }
                        }
                    }

                    // Support pour FileParam: bind UploadedFile even if a string param was present
                    RequestParam fp = parameters[i].getAnnotation(RequestParam.class);
                    if (fp != null && parameters[i].getType() == UploadedFile.class) {
                        try {
                            Part filePart = request.getPart(fp.value());
                            if (filePart != null) {
                                String submittedName = filePart.getSubmittedFileName();
                                if (submittedName != null && !submittedName.trim().isEmpty()) {
                                    UploadedFile uploadedFile = new UploadedFile(
                                        submittedName,
                                        filePart.getContentType(),
                                        filePart.getSize(),
                                        filePart.getInputStream()
                                    );
                                    args[i] = uploadedFile;
                                } else {
                                    System.out.println("Fichier présent mais nom vide pour param: " + fp.value());
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Erreur lors de l'extraction du fichier " + fp.value() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }

            Object instance = methodClassMapping
                    .get(method)
                    .getDeclaredConstructor()
                    .newInstance();

            Object result = method.invoke(instance, args);
            if (method.isAnnotationPresent(JsonResponse.class)) {
                Gson gson = new Gson();
                return gson.toJson(result);
            } else {
                return result;
            }
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
