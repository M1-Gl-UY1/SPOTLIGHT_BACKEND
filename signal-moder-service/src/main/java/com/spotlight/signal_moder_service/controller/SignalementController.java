package com.spotlight.signal_moder_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spotlight.signal_moder_service.enums.StatutStignalement;
import com.spotlight.signal_moder_service.enums.TypeCible;
import com.spotlight.signal_moder_service.models.Signalement;
import com.spotlight.signal_moder_service.repository.SignalementRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/signalements")
@RequiredArgsConstructor
public class SignalementController {

    private final SignalementRepository signalementRepository;

    // 1. CRÉER UN SIGNALEMENT (Utilisé par le Client)
    @PostMapping
    public ResponseEntity<Signalement> creerSignalement(@Valid @RequestBody Signalement signalement) {
        // Par défaut, le statut est défini à EN_ATTENTE via l'entité (@PrePersist)
        Signalement nouveau = signalementRepository.save(signalement);
        return ResponseEntity.ok(nouveau);
    }

    // 2. VOIR LES SIGNALEMENTS EN ATTENTE (Utilisé par l'Admin)
    @GetMapping("/en-attente")
    public ResponseEntity<List<Signalement>> getSignalementsEnAttente() {
        return ResponseEntity.ok(signalementRepository.findByStatut(StatutStignalement.EN_ATTENTE));
    }

    // 3. VOIR L'HISTORIQUE D'UNE CIBLE (Utilisé par l'Admin)
    // Ex: Voir toutes les fois où ce prestataire a été signalé
    @GetMapping("/historique")
    public ResponseEntity<List<Signalement>> getHistoriqueCible(
            @RequestParam Long cibleId, 
            @RequestParam TypeCible typeCible) {
        return ResponseEntity.ok(signalementRepository.findByCibleIdAndTypeCible(cibleId, typeCible));
    }
}