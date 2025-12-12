package com.spotlight.signal_moder_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.spotlight.signal_moder_service.dto.ModerationRequest;
import com.spotlight.signal_moder_service.dto.ResolutionLitigeRequest; // À créer (voir plus bas)
import com.spotlight.signal_moder_service.models.Moderation;
import com.spotlight.signal_moder_service.service.ModerationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1") // Préfixe commun
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;

    // 1. APPLIQUER UNE SANCTION (Orchestration)
    // URL: POST /api/v1/moderation/apply
    @PostMapping("/moderation/apply")
    public ResponseEntity<Moderation> appliquerSanction(@RequestBody ModerationRequest request) {
        Moderation moderation = moderationService.appliquerDecision(
                request.getAdminId(),
                request.getSignalementId(),
                request.getAction(), // SUSPENDRE_COMPTE, SUSPENDRE_SERVICE...
                request.getJustification(),
                request.getDateFinSuspension()
        );
        return ResponseEntity.ok(moderation);
    }

    // 2. RÉSOUDRE UN LITIGE (Remboursement / Paiement)
    // URL: POST /api/v1/admin/litiges/{id}/resolve
    @PostMapping("/admin/litiges/{litigeId}/resolve")
    public ResponseEntity<String> resoudreLitige(@PathVariable Long litigeId, 
                                                 @RequestBody ResolutionLitigeRequest request) {
        
        // Cette méthode va appeler le service OFFERS-SERVICE pour débloquer les fonds
        moderationService.resoudreLitige(
            litigeId,
            request.getDecision(), // "REMBOURSER_CLIENT" ou "PAYER_PRESTATAIRE"
            request.getJustification()
        );

        return ResponseEntity.ok("Litige " + litigeId + " résolu avec la décision : " + request.getDecision());
    }
}