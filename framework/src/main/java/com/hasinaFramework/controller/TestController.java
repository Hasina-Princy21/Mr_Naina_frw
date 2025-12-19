package com.hasinaFramework.controller;

import com.hasinaFramework.annotation.Controller;
import com.hasinaFramework.annotation.GetMapping;
import com.hasinaFramework.annotation.RequestParam;
// import com.hasinaFramework.annotation.UrlServlet;

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
    public String helloWith(@RequestParam("id") int id){
        return "hello " + id;
    }
}
