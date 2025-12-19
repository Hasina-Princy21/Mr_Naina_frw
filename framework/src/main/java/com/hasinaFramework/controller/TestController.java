package com.hasinaFramework.controller;

import java.util.Map;

import com.hasinaFramework.annotation.Controller;
import com.hasinaFramework.annotation.GetMapping;
// import com.hasinaFramework.annotation.PostMapping;

@Controller
public class TestController {
    // @UrlServlet("/test")
    // public String testMethod() {
    //     return "method test";
    // }

    // @UrlServlet("/hello")
    // public String helloMethod(@RequestParam("prenom") String nom) {
    //     return "Hello " + nom;
    // }

    // @UrlServlet("/hello/{id}")
    // public String helloMethod(int id) {
    //     return "Hello " + id;
    // }

    @GetMapping("/hello")
    public String hello(){
        return "Hello from get";
    }

    @GetMapping("/hello/{id}")
    public String helloWith(Map<String, String> params){
        String id = params.get("id");
        return "hello " + id;
    }

    // @UrlServlet("/hello/{id}")
    // public String helloMethod(int id) {
    //     return "Hello " + id;
    // }
}
