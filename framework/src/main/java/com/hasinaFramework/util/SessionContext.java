package com.hasinaFramework.util;

import java.util.Map;

public class SessionContext {
    
    private static final Map<String, Object> sessionMap = new java.util.HashMap<>();

    public static Map<String, Object> getSessionMap() {
        return sessionMap;
    }

}
