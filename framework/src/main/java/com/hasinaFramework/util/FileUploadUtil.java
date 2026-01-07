package com.hasinaFramework.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUploadUtil {
    private String uploadDir;

    public FileUploadUtil(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    /**
     * Sauvegarde un fichier uploadé
     * @param fileName le nom du fichier
     * @param inputStream le flux d'entrée du fichier
     * @return le chemin du fichier sauvegardé
     */
    public String saveFile(String fileName, InputStream inputStream) throws Exception {
        Path uploadPath = Paths.get(uploadDir);
        
        // Créer le répertoire s'il n'existe pas
        Files.createDirectories(uploadPath);
        
        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(inputStream, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        return filePath.toString();
    }

    /**
     * Récupère les bytes d'un fichier uploadé
     * @param fileName le nom du fichier
     * @param inputStream le flux d'entrée du fichier
     * @return les bytes du fichier
     */
    public byte[] getFileBytes(InputStream inputStream) throws Exception {
        return inputStream.readAllBytes();
    }

    /**
     * Supprime un fichier
     * @param filePath le chemin du fichier
     */
    public void deleteFile(String filePath) throws Exception {
        Files.delete(Paths.get(filePath));
    }
}
