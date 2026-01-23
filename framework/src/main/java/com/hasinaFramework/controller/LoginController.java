package com.hasinaFramework.controller;

import com.hasinaFramework.ModelVue;
import com.hasinaFramework.annotation.Controller;
import com.hasinaFramework.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/")
    public ModelVue login(){
        ModelVue v = new ModelVue("login.jsp");
        return v;
    }

}
