package com.hasinaFramework;

@Controller
public class EtudiantController {
    @UrlServlet("/etudiants")
    public String getEtudiant(){
        return "Listes etudiants";
    }
}
