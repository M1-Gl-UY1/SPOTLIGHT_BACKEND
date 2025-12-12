package com.spotlight.offerandprestation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.spotlight.offerandprestation.repository.AvisRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/prestataires")
public class InternalDataController {

    @Autowired
    private AvisRepository avisRepository;

    // GET /prestataires/{id}/rating-details
    // Cette route sera appelée par le service CATALOGUE ou USER pour mettre à jour le profil
    @GetMapping("/{id}/rating-details")
    public ResponseEntity<Map<String, Object>> getProviderRatingDetails(@PathVariable Long id) {
        Double moyenne = avisRepository.getMoyenneGlobalePrestataire(id);
        Long count = avisRepository.countAvisGlobauxPrestataire(id);

        Map<String, Object> response = new HashMap<>();
        response.put("prestataireId", id);
        response.put("globalRating", moyenne != null ? moyenne : 0.0);
        response.put("totalReviews", count);

        return ResponseEntity.ok(response);
    }
}
