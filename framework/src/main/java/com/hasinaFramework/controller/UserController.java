package com.hasinaFramework.controller;

import java.util.List;
import java.util.Map;

import com.hasinaFramework.ModelVue;
import com.hasinaFramework.annotation.Authorized;
import com.hasinaFramework.annotation.Controller;
import com.hasinaFramework.annotation.GetMapping;
import com.hasinaFramework.annotation.JsonResponse;
import com.hasinaFramework.annotation.PostMapping;
import com.hasinaFramework.annotation.RequestParam;
import com.hasinaFramework.annotation.Role;
import com.hasinaFramework.annotation.Session;
import com.hasinaFramework.model.User;
import com.hasinaFramework.service.UserService;

@Controller
public class UserController {
    private UserService userService = new UserService();

    public boolean cheklogin(String email, String password){
        boolean res = false;
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                res = true;
            } 
        }
        return res;
    }

    @GetMapping("/users")
    @JsonResponse
    @Authorized
    public List<User> users(){
        List<User> users = userService.getAllUsers();
        return users;
    }

    @GetMapping("/admin")
    @Role("admin")
    public ModelVue adminPage(@Session Map<String, Object> session){
        ModelVue v = new ModelVue("admin.jsp");
        return v;
    }

    @PostMapping("/login")
    public String singin(@RequestParam("email") String email, 
            @RequestParam("password") String password,
            @Session Map<String, Object> session){
        User user = userService.getUserByEmailAndPassword(email, password);
        
        // Vérifier si l'utilisateur existe avant de mettre dans la session
        if (user != null) {
            // L'utilisateur est authentifié, mettre l'ID et le rôle dans la session
            session.put("user", user.getId());
            session.put("role", user.getRole()); // IMPORTANT : stocker le rôle pour @Role
            return "Connexion réussie pour : " + user.getEmail();
        } else {
            // L'utilisateur n'existe pas ou mot de passe incorrect
            return "Email ou mot de passe incorrect";
        }
    }
}
