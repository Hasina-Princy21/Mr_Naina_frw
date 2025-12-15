package com.hasinaFramework.controller;

import com.hasinaFramework.annotation.Controller;
import com.hasinaFramework.annotation.UrlServlet;

@Controller
public class TestController {
    @UrlServlet("/test")
    public String testMethod() {
        return "method test";
    }

    @UrlServlet("/hello")
    public String helloMethod( String nom) {
        return "Hello " + nom;
    }
}
