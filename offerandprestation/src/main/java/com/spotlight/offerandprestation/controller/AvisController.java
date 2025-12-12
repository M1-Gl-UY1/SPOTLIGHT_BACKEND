package com.spotlight.offerandprestation.controller;

import com.spotlight.offerandprestation.dto.AvisDTO;
import com.spotlight.offerandprestation.dto.DtoMapper;
import com.spotlight.offerandprestation.models.Avis;
import com.spotlight.offerandprestation.services.AvisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Importe tout (y compris RequestBody correct)

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1") // On garde la base v1 ici
public class AvisController {

    @Autowired
    private AvisService avisService;
    @Autowired
    private DtoMapper mapper;

    // --- 1. NOTATION D'UNE COMMANDE (POST /api/v1/orders/{id}/ratings) ---
    // Cette route respecte le contrat d'interface défini plus tôt
    @PostMapping("/orders/{commandeId}/ratings")
    public ResponseEntity<AvisDTO> rateOrder(@PathVariable Long commandeId, 
                                             @RequestBody AvisDTO avisDto) {
        // On utilise l'objet DTO (JSON) au lieu des RequestParam
        Avis avis = avisService.ajouterAvis(commandeId, avisDto.getNote(), avisDto.getCommentaire());
        return ResponseEntity.ok(mapper.toAvisDTO(avis));
    }

    // --- 2. CONSULTATION DES AVIS D'UN SERVICE ---
    // GET /api/v1/services/{serviceId}/reviews (Standardisation URL en anglais/cohérent)
    // Ou si tu préfères : /api/v1/avis/service/{serviceId}
    @GetMapping("/services/{serviceId}/reviews")
    public ResponseEntity<List<AvisDTO>> getAvisService(@PathVariable Long serviceId) {
        List<AvisDTO> avisList = avisService.getAvisByService(serviceId).stream()
                .map(mapper::toAvisDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(avisList);
    }
}