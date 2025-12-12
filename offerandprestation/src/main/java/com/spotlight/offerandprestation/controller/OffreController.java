package com.spotlight.offerandprestation.controller;

import com.spotlight.offerandprestation.dto.DtoMapper;
import com.spotlight.offerandprestation.dto.MediaDTO;
import com.spotlight.offerandprestation.dto.ServiceOffreDTO;
import com.spotlight.offerandprestation.models.Media;
import com.spotlight.offerandprestation.models.ServiceOffre;
import com.spotlight.offerandprestation.services.OffreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/services") // Renommé 'services' pour cohérence avec 'orders'
public class OffreController {

    @Autowired
    private OffreService service;

    @Autowired
    private DtoMapper mapper;

    // POST /api/v1/services
    @PostMapping
    public ResponseEntity<ServiceOffreDTO> createService(@RequestBody ServiceOffre s) {
        ServiceOffre created = service.creerService(s);
        return ResponseEntity.ok(mapper.toServiceDTO(created));
    }

    // GET /api/v1/services
    @GetMapping
    public ResponseEntity<List<ServiceOffreDTO>> getAll() {
        List<ServiceOffreDTO> list = service.getAllServices().stream()
                .map(mapper::toServiceDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // GET /api/v1/services/prestataire/{id}
    @GetMapping("/prestataire/{id}")
    public ResponseEntity<List<ServiceOffreDTO>> getByPrestataire(@PathVariable Long id) {
        List<ServiceOffreDTO> list = service.getServicesByPrestataire(id).stream()
                .map(mapper::toServiceDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
    
    // POST /api/v1/services/{id}/medias
    @PostMapping("/{id}/medias")
    public ResponseEntity<MediaDTO> uploadMedia(@PathVariable Long id, 
                                                @RequestParam("file") MultipartFile file,
                                                @RequestParam("type") String type) {
        Media media = service.ajouterMedia(id, file, type);
        return ResponseEntity.ok(mapper.toMediaDTO(media));
    }
}