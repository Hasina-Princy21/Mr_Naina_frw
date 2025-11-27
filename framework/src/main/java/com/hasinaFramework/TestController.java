package com.hasinaFramework;

@Controller
public class TestController {
    @UrlServlet("/test")
    public String testMethod() {
        return "method test";
    }

    @UrlServlet("/hello")
    public String helloMethod() {
        return "method hello";
    }
}
