package com.spotlight.offerandprestation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.spotlight.offerandprestation.dto.AvisDTO;
import com.spotlight.offerandprestation.dto.DtoMapper;
import com.spotlight.offerandprestation.models.Avis;
import com.spotlight.offerandprestation.services.AvisService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/avis")
public class AvisController {

    @Autowired
    private AvisService avisService;
    @Autowired
    private DtoMapper mapper;

    // Use Case : Le client note une commande
    @PostMapping("/commande/{commandeId}")
    public AvisDTO noterCommande(@PathVariable Long commandeId, 
                                 @RequestParam int note, 
                                 @RequestParam String commentaire) {
        Avis avis = avisService.ajouterAvis(commandeId, note, commentaire);
        return mapper.toAvisDTO(avis);
    }

    // Use Case : Voir les avis sur un service (pour la page détails)
    @GetMapping("/service/{serviceId}")
    public List<AvisDTO> getAvisService(@PathVariable Long serviceId) {
        return avisService.getAvisByService(serviceId).stream()
                .map(mapper::toAvisDTO)
                .collect(Collectors.toList());
    }
}