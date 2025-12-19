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

    // @UrlServlet("/etudiant")
    // public String getEtudiantById(int id){
    //     return "id : " + id;
    // }
    
}
