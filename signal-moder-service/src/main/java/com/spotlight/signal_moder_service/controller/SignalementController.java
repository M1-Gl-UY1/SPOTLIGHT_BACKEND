package com.spotlight.signal_moder_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.spotlight.signal_moder_service.enums.StatutStignalement;
import com.spotlight.signal_moder_service.models.Signalement;
import com.spotlight.signal_moder_service.repository.SignalementRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
// Pas de RequestMapping global ici car les routes sont mixtes (Public vs Admin)
@RequiredArgsConstructor
public class SignalementController {

    private final SignalementRepository signalementRepository;

    // --- PARTIE CLIENT ---
    
    // 1. CRÉER UN SIGNALEMENT (Utilisé par le Client)
    // URL: POST /api/v1/reports
    @PostMapping("/api/v1/reports")
    public ResponseEntity<Signalement> creerSignalement(@Valid @RequestBody Signalement signalement) {
        Signalement nouveau = signalementRepository.save(signalement);
        return ResponseEntity.ok(nouveau);
    }

    // --- PARTIE ADMIN (Conforme au tableau) ---

    // 2. LISTER LES SIGNALEMENTS (Admin)
    // URL: GET /api/v1/admin/reports
    @GetMapping("/api/v1/admin/reports")
    public ResponseEntity<List<Signalement>> getAllSignalements(
            @RequestParam(required = false) StatutStignalement statut) {
        
        if (statut != null) {
            return ResponseEntity.ok(signalementRepository.findByStatut(statut));
        }
        return ResponseEntity.ok(signalementRepository.findAll());
    }
}