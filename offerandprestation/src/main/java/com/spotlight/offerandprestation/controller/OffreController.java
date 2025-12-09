package com.spotlight.offerandprestation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.spotlight.offerandprestation.dto.DtoMapper;
import com.spotlight.offerandprestation.dto.MediaDTO;
import com.spotlight.offerandprestation.dto.ServiceOffreDTO;
import com.spotlight.offerandprestation.models.Media;
import com.spotlight.offerandprestation.models.ServiceOffre;
import com.spotlight.offerandprestation.services.OffreService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
// @CrossOrigin("*") // Décommente si tu as des erreurs CORS avec React
public class OffreController {

    @Autowired
    private OffreService service;

    @Autowired
    private DtoMapper mapper;

    // Création d'un service (Le prestataire envoie le JSON)
    @PostMapping
    public ServiceOffreDTO createService(@RequestBody ServiceOffre s) {
        ServiceOffre created = service.creerService(s);
        return mapper.toServiceDTO(created);
    }

    // Récupérer tous les services (Pour la page d'accueil)
    @GetMapping
    public List<ServiceOffreDTO> getAll() {
        return service.getAllServices().stream()
                .map(mapper::toServiceDTO)
                .collect(Collectors.toList());
    }

    // Récupérer les services d'un prestataire spécifique (Pour son profil)
    @GetMapping("/prestataire/{id}")
    public List<ServiceOffreDTO> getByPrestataire(@PathVariable Long id) {
        return service.getServicesByPrestataire(id).stream()
                .map(mapper::toServiceDTO)
                .collect(Collectors.toList());
    }
    
    // Upload d'une image/vidéo pour un service
    @PostMapping("/{id}/medias")
    public MediaDTO uploadMedia(@PathVariable Long id, 
                                @RequestParam("file") MultipartFile file,
                                @RequestParam("type") String type) {
        Media media = service.ajouterMedia(id, file, type);
        return mapper.toMediaDTO(media);
    }
}