package com.hasinaFramework.controller;

import java.util.Map;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import com.hasinaFramework.ModelVue;
import com.hasinaFramework.annotation.Controller;
// import com.hasinaFramework.annotation.FileParam;
import com.hasinaFramework.annotation.GetMapping;
import com.hasinaFramework.annotation.PostMapping;
import com.hasinaFramework.annotation.RequestParam;
import com.hasinaFramework.annotation.Session;
import com.hasinaFramework.util.FileUploadUtil;
import com.hasinaFramework.util.UploadedFile;

@Controller
public class TestController {

    @GetMapping("/")
    public ModelVue home(){
        ModelVue v = new ModelVue("home.jsp");
        return v;
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello from get";
    }

    @GetMapping("/hello/{id}")
    public String helloWith(Map<String, String> params){
        String id = params.get("id");
        return "hello " + id;
    }

    @GetMapping("/upload")
    public ModelVue upload (){
        ModelVue v = new ModelVue("upload.jsp");
        return v;
    }

    @PostMapping("/upload/save")
    public String uploadSave (@RequestParam("file") UploadedFile file){
        if (file == null) {
            return "Aucun fichier uploadé";
        }

        System.out.println("[UPLOAD] Received UploadedFile: fileName=" + file.getFileName() + ", size=" + file.getSize());

        if (file.getFileName() == null || file.getFileName().isEmpty()) {
            return "Aucun nom de fichier";
        }

        try {
            InputStream is = file.getInputStream();
            if (is == null) {
                byte[] b = null;
                try {
                    b = file.getBytes();
                } catch (Exception ex) {
                    // ignore
                }
                if (b == null || b.length == 0) {
                    return "Flux du fichier introuvable";
                }
                is = new ByteArrayInputStream(b);
            }

            FileUploadUtil fileUtil = new FileUploadUtil("uploads");
            String filePath = fileUtil.saveFile(file.getFileName(), is);

            return "Fichier uploadé avec succès: " + file.getFileName() + 
                   " (Taille: " + file.getSize() + " bytes, Chemin: " + filePath + ")";
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            return "Erreur lors de l'upload: " + msg;
        }
    }

    // Exemple d'utilisation de @Session
    // @GetMapping("/session/get")
    // public String setSession(@RequestParam("key") String key, 
    //                         @RequestParam("value") String value, 
    //                         @Session Map<String, Object> session) {
    //     session.put(key, value);
    //     return "Valeur '" + value + "' stockée dans la session avec la clé '" + key + "'";
    // }

    @GetMapping("/session")
    public ModelVue sessionDemo() {
        ModelVue v = new ModelVue("session.jsp");
        return v;
    }

    @PostMapping("/session/get")
    public String getSession(@RequestParam("value") String value, 
                            @Session Map<String, Object> session) {
        session.put("user", value);
        return "Valeur pour ' user: ' " + session.get("user");
    }

    // @GetMapping("/session/counter")
    // public String sessionCounter(@Session Map<String, Object> session) {
    //     Integer counter = (Integer) session.get("counter");
    //     if (counter == null) {
    //         counter = 0;
    //     }
    //     counter++;
    //     session.put("counter", counter);
    //     return "Compteur de visites: " + counter;
    // }
}

