package com.spotlight.offerandprestation.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.util.StringUtils;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    // On récupère le chemin depuis application.properties
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Impossible de créer le dossier de stockage.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Nettoyer le nom du fichier
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        // Générer un nom unique pour éviter les conflits (ex: uuid_monimage.jpg)
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            // Vérification de sécurité simple
            if(fileName.contains("..")) {
                throw new RuntimeException("Nom de fichier invalide " + fileName);
            }

            // Copie du fichier dans le dossier cible
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Impossible de stocker le fichier " + fileName, ex);
        }
    }
}