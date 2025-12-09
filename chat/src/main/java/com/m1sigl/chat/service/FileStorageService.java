package com.m1sigl.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    private final String UPLOAD_DIR = "uploads/";

    public String saveFile(MultipartFile file) throws IOException{
        // 1. Créer le dossier s'il n'existe pas
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()){
            directory.mkdirs();
        }

        // 2. Générer un nom unique pour éviter les écrasements (uuid_chat.jpg)
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        // 3. Sauvegarder le fichier sur le disque
        Path filePath = Paths.get(UPLOAD_DIR + uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        // 4. Retourner l'URL relative (accessible via le navigateur)
        return uniqueFilename;
    }


}