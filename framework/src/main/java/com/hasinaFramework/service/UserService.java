package com.hasinaFramework.service;

import com.hasinaFramework.model.User;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private List<User> users = new ArrayList<>();

    public UserService() {
        // Charge le fichier qui est dans le même package/dossier
        loadUsersFromResources("base.csv");
    }

    private void loadUsersFromResources(String fileName) {
        // Cette méthode cherche le fichier au même endroit que la classe UserService
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            if (is == null) {
                System.err.println("Erreur : Fichier " + fileName + " non trouvé à côté de la classe.");
                return;
            }

            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }

                String[] data = line.split(";");
                if (data.length >= 4) {
                    try {
                        int id = Integer.parseInt(data[0].trim());
                        String email = data[1].trim();
                        String password = data[2].trim();
                        String role = data[3].trim();
                        users.add(new User(id, email, password, role));
                    } catch (NumberFormatException e) {
                        // Ignore les lignes où l'ID n'est pas un nombre
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<User> getAllUsers() {
        return users;
    }

    public User getUserByEmailAndPassword(String email, String password){
        return users.stream()
            .filter(u -> u.getEmail().equals(email) && u.getPassword().equals(password))
            .findFirst()
            .orElse(null);
    }

    public User getUserById(int id) {
        return users.stream()
            .filter(u -> u.getId() == id)
            .findFirst()
            .orElse(null);
    }
}