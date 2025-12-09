package com.spotlight.offerandprestation.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.spotlight.offerandprestation.models.Media;
import com.spotlight.offerandprestation.models.Pack;
import com.spotlight.offerandprestation.models.ServiceOffre;
import com.spotlight.offerandprestation.repository.MediaRepository;
import com.spotlight.offerandprestation.repository.ServiceOffreRepository;

@Service
public class OffreService {
    @Autowired
    private ServiceOffreRepository repository;
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private MediaRepository mediaRepository;

    public ServiceOffre creerService(ServiceOffre service) {
        // Logique IA pour améliorer la description pourrait être insérée ici
        
        // On lie les packs au service pour la persistence
        if(service.getPacks() != null){
            for(Pack p : service.getPacks()){
                p.setServiceOffre(service);
            }
        }
        return repository.save(service);
    }

    public List<ServiceOffre> getServicesByPrestataire(Long id) {
        return repository.findByPrestataireId(id);
    }
    
    public List<ServiceOffre> getAllServices() {
        return repository.findAll();
    }

    public Media ajouterMedia(Long serviceId, MultipartFile file, String type) {
        ServiceOffre service = repository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service introuvable"));

        // 1. Stocker le fichier physiquement
        String fileName = fileStorageService.storeFile(file);

        // 2. Créer l'URL d'accès (ex: http://localhost:8081/uploads/nom_fichier.jpg)
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(fileName)
                .toUriString();

        // 3. Sauvegarder en Base de données
        Media media = new Media();
        media.setUrl(fileDownloadUri);
        media.setType(type); // "IMAGE" ou "VIDEO"
        media.setServiceOffre(service);

        return mediaRepository.save(media);
    }
}
