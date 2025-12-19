package com.hasinaFramework.controller;

import com.hasinaFramework.ModelVue;
import com.hasinaFramework.annotation.Controller;
import com.hasinaFramework.annotation.GetMapping;
import com.hasinaFramework.annotation.PostMapping;
import com.hasinaFramework.annotation.RequestParam;
// import com.hasinaFramework.annotation.UrlServlet;

@Controller
public class EtudiantController {
    @GetMapping("/etudiants")
    public ModelVue getEtudiant(){
        ModelVue mv = new ModelVue("listeEtudiants.jsp");
        mv.addData("titre", "Liste des étudiants");
        mv.addData("nbEtudiants", 45);
        mv.addData("message", "Bienvenue dans la liste");
        return mv;
    }

    @GetMapping("/etudiant")
    public ModelVue formEtudiant(){
        ModelVue mv = new ModelVue("formEtudiant.jsp");
        return mv;
    }

    @PostMapping("/etu/save")
    public String save (@RequestParam("nom") String nom ) {
        return "Nom: " + nom ;
    }
    
}
