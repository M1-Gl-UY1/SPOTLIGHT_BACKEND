package com.spotlight.offerandprestation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.spotlight.offerandprestation.dto.CommandeDTO;
import com.spotlight.offerandprestation.dto.DtoMapper;
import com.spotlight.offerandprestation.enums.StatutCommande;
import com.spotlight.offerandprestation.models.Commande;
import com.spotlight.offerandprestation.repository.CommandeRepository;
import com.spotlight.offerandprestation.services.CommandeService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/orders") // 1. Route conforme à la spec
public class OrderController { // Renommé pour cohérence (ou garde CommandeController)

    @Autowired
    private CommandeService commandeService;

    @Autowired
    private CommandeRepository commandeRepo;

    @Autowired
    private DtoMapper mapper;

    // --- 1. CRÉATION DE COMMANDE ---
    // POST /api/v1/orders
    @PostMapping
    public ResponseEntity<CommandeDTO> createOrder(@RequestParam Long clientId, @RequestParam Long packId) {
        Commande c = commandeService.passerCommande(clientId, packId);
        return ResponseEntity.ok(mapper.toCommandeDTO(c));
    }

    // --- 2. MISE À JOUR DU STATUT (Générique) ---
    // PATCH /api/v1/orders/{id}/status
    // Cette méthode remplace 'demarrer', 'livrer' et 'valider' pour respecter le contrat.
    // Body attendu : { "status": "LIVRE", "url": "http://..." } (url optionnel sauf si LIVRE)
    @PatchMapping("/{id}/status")
    public ResponseEntity<CommandeDTO> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String statusStr = payload.get("status");
        StatutCommande newStatut = StatutCommande.valueOf(statusStr); // EN_COURS, LIVRE, TERMINE...

        Commande c;

        // Cas spécial : Si on livre, il faut l'URL du fichier
        if (newStatut == StatutCommande.LIVRE && payload.containsKey("url")) {
            String urlLivrable = payload.get("url");
            c = commandeService.livrerCommande(id, urlLivrable);
        } else {
            // Cas standard : Démarrage ou Validation
            c = commandeService.changerStatut(id, newStatut);
        }

        return ResponseEntity.ok(mapper.toCommandeDTO(c));
    }

    // --- 3. PAIEMENT ---
    // POST /api/v1/orders/{id}/payment
    @PostMapping("/{id}/payment")
    public ResponseEntity<String> processPayment(@PathVariable Long id) {
        // Logique : Appel au service de paiement, ou validation du séquestre
        // Ici on simule le succès
        return ResponseEntity.ok("Paiement effectué/séquestré avec succès pour la commande " + id);
    }

    // --- 4. CONSULTATION (Routes conservées mais préfixées par /api/v1/orders) ---

    // GET /api/v1/orders/client/{clientId}
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CommandeDTO>> getCommandesByClient(@PathVariable Long clientId) {
        List<CommandeDTO> list = commandeRepo.findByClientId(clientId).stream()
                .map(mapper::toCommandeDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // GET /api/v1/orders/prestataire/{prestataireId}
    @GetMapping("/prestataire/{prestataireId}")
    public ResponseEntity<List<CommandeDTO>> getCommandesByPrestataire(@PathVariable Long prestataireId) {
        List<CommandeDTO> list = commandeRepo.findByPack_ServiceOffre_PrestataireId(prestataireId).stream()
                .map(mapper::toCommandeDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}