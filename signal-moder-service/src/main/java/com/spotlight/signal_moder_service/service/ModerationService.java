package com.spotlight.signal_moder_service.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.spotlight.signal_moder_service.client.CatalogServiceClient;
import com.spotlight.signal_moder_service.client.ChatServiceClient;
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

    // Injection des clients Feign pour parler aux autres microservices
    private final UserServiceClient userServiceClient;
    private final CatalogServiceClient catalogServiceClient;
    private final ReviewServiceClient reviewServiceClient;
    private final ChatServiceClient chatServiceClient;

    /**
     * Méthode principale appelée par le Contrôleur quand l'Admin prend une décision
     */
    @Transactional
    public Moderation appliquerDecision(Long adminId, Long signalementId, TypeAction action, String justification, LocalDateTime dateFinSuspension) {
        
        // 1. Récupérer le signalement
        Signalement signalement = signalementRepo.findById(signalementId)
                .orElseThrow(() -> new RuntimeException("Signalement introuvable"));

        // 2. Créer l'objet Moderation (Historique de la décision)
        Moderation decision = new Moderation();
        decision.setAdminId(adminId);
        decision.setSignalement(signalement);
        decision.setTypeAction(action); // Supposant que tu as renommé TypeDecision en TypeAction dans l'entité Moderation
        decision.setJustification(justification);
        decision.setDateFinSuspension(dateFinSuspension);
        
        // 3. Exécuter l'action technique vers les autres microservices
        executerActionTechnique(action, signalement.getCibleId(), dateFinSuspension);

        // 4. Mettre à jour le statut du signalement
        signalement.setStatut(StatutStignalement.TRAITE);
        signalementRepo.save(signalement); // Sauvegarde statut
        
        return moderationRepo.save(decision); // Sauvegarde décision
    }

    /**
     * Logique de dispatch vers les bons microservices
     */
    private void executerActionTechnique(TypeAction action, Long cibleId, LocalDateTime dateFin) {
        switch (action) {
            case SUSPENSION_CLIENT:
            case SUSPENSION_PRESTATAIRE:
                // On convertit la date en String ou on gère null selon l'API du User Service
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
                // Ici, on pourrait utiliser RabbitMQ pour envoyer un email/notif
                System.out.println("TODO: Envoyer un email d'avertissement à l'utilisateur " + cibleId);
                break;
                
            default:
                throw new IllegalArgumentException("Action non supportée : " + action);
        }
    }
}