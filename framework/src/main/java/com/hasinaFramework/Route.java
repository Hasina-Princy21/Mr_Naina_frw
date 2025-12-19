package com.hasinaFramework;

import java.lang.reflect.Method;

public class Route {
    private String path;
    private String httpMethod;
    private Method method;
    private Class<?> controller;

    public Route(String path, String httpMethod, Method method, Class<?> controller) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.method = method;
        this.controller = controller;
    }

    public String getPath() { return path; }
    public String getHttpMethod() { return httpMethod; }
    public Method getMethod() { return method; }
    public Class<?> getController() { return controller; }
}

