package com.hasinaFramework;

@Controller
public class EtudiantController {
    @UrlServlet("/etudiants")
    public ModelVue getEtudiant(){
        ModelVue mv = new ModelVue("listeEtudiants.jsp");
        mv.addData("titre", "Liste des étudiants");
        mv.addData("nbEtudiants", 45);
        mv.addData("message", "Bienvenue dans la liste");
        return mv;
    }
}
