package com.spotlight.signal_moder_service.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.spotlight.signal_moder_service.client.CatalogServiceClient;
import com.spotlight.signal_moder_service.client.ChatServiceClient;
import com.spotlight.signal_moder_service.client.OfferServiceClient; 
import com.spotlight.signal_moder_service.client.ReviewServiceClient;
import com.spotlight.signal_moder_service.client.UserServiceClient;
import com.spotlight.signal_moder_service.enums.StatutStignalement;
import com.spotlight.signal_moder_service.models.Moderation;
import com.spotlight.signal_moder_service.models.Signalement;
import com.spotlight.signal_moder_service.models.TypeAction;
import com.spotlight.signal_moder_service.repository.ModerationRepository;
import com.spotlight.signal_moder_service.repository.SignalementRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final SignalementRepository signalementRepo;
    private final ModerationRepository moderationRepo;

    // --- Clients Feign ---
    private final UserServiceClient userServiceClient;
    private final CatalogServiceClient catalogServiceClient;
    private final ReviewServiceClient reviewServiceClient;
    private final ChatServiceClient chatServiceClient;
    private final OfferServiceClient offerServiceClient; // Pour les litiges

    /**
     * CAS 1 : Appliquer une sanction (Ban, Suppression...)
     */
    @Transactional
    public Moderation appliquerDecision(Long adminId, Long signalementId, TypeAction action, String justification, LocalDateTime dateFinSuspension) {
        
        Signalement signalement = signalementRepo.findById(signalementId)
                .orElseThrow(() -> new RuntimeException("Signalement introuvable"));

        Moderation decision = new Moderation();
        decision.setAdminId(adminId);
        decision.setSignalement(signalement);
        decision.setTypeAction(action);
        decision.setJustification(justification);
        decision.setDateFinSuspension(dateFinSuspension);
        
        // Exécution technique
        executerActionTechnique(action, signalement.getCibleId(), dateFinSuspension);

        // Clôture du signalement
        signalement.setStatut(StatutStignalement.TRAITE);
        signalementRepo.save(signalement);
        
        return moderationRepo.save(decision);
    }

    /**
     * CAS 2 : Résoudre un Litige (Remboursement / Paiement)
     * Cette méthode est appelée par la nouvelle route /admin/litiges/{id}/resolve
     */
    public void resoudreLitige(Long litigeId, String decision, String justification) {
        Signalement litige = signalementRepo.findById(litigeId)
                .orElseThrow(() -> new RuntimeException("Litige introuvable"));

        Long commandeId = litige.getCibleId();
        
        // Préparation du body pour l'appel API vers OFFERS-SERVICE
        Map<String, String> payload = new HashMap<>();

        if ("REFUND_CLIENT".equalsIgnoreCase(decision)) {
            // Si on rembourse le client, on annule la commande
            payload.put("status", "ANNULE"); 
        } else if ("RELEASE_PAYMENT".equalsIgnoreCase(decision)) {
            // Si on paie le prestataire, on termine la commande
            payload.put("status", "TERMINE");
        } else {
            throw new IllegalArgumentException("Décision de litige inconnue : " + decision);
        }

        // Appel distant vers le microservice OFFERS
        offerServiceClient.updateOrderStatus(commandeId, payload);

        // Clôture locale
        litige.setStatut(StatutStignalement.TRAITE);
        signalementRepo.save(litige);
        
        Moderation mod = new Moderation();
        mod.setSignalement(litige);
        mod.setJustification("Résolution litige: " + decision + " - " + justification);
        mod.setTypeAction(TypeAction.RESOLUTION_LITIGE); 
        moderationRepo.save(mod);
    }

    private void executerActionTechnique(TypeAction action, Long cibleId, LocalDateTime dateFin) {
        switch (action) {
            case SUSPENSION_CLIENT:
            case SUSPENSION_PRESTATAIRE:
                String dateStr = (dateFin != null) ? dateFin.toString() : null;
                userServiceClient.changerStatutUtilisateur(cibleId, "SUSPENDU", dateStr);
                break;

            case ANNULATION_SERVICE:
                catalogServiceClient.supprimerOuMasquerService(cibleId);
                break;

            case SUPPRESSION_COMMENTAIRE:
                reviewServiceClient.supprimerCommentaire(cibleId);
                break;

            case SUPPRESSION_TCHAT_MESSAGE:
                chatServiceClient.supprimerMessage(cibleId);
                break;

            case AVERTISSEMENT_CLIENT:
            case AVERTISSEMENT_PRESTATAIRE:
                // Notification (via RabbitMQ idéalement)
                break;
                
            default:
                // Pour RESOLUTION_LITIGE, on ne fait rien ici car c'est géré par resoudreLitige
                if (action != TypeAction.RESOLUTION_LITIGE) {
                    throw new IllegalArgumentException("Action non supportée : " + action);
                }
        }
    }
}