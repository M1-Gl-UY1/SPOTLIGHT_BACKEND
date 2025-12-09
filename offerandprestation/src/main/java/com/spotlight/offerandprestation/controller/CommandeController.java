package com.spotlight.offerandprestation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.spotlight.offerandprestation.dto.CommandeDTO;
import com.spotlight.offerandprestation.dto.DtoMapper;
import com.spotlight.offerandprestation.enums.StatutCommande;
import com.spotlight.offerandprestation.models.Commande;
import com.spotlight.offerandprestation.repository.CommandeRepository;
import com.spotlight.offerandprestation.services.CommandeService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    @Autowired
    private CommandeService commandeService;
    
    @Autowired
    private CommandeRepository commandeRepo; // Injection directe pour les lectures simples

    @Autowired
    private DtoMapper mapper;

    // 1. Client passe commande
    @PostMapping
    public CommandeDTO creerCommande(@RequestParam Long clientId, @RequestParam Long packId) {
        Commande c = commandeService.passerCommande(clientId, packId);
        return mapper.toCommandeDTO(c);
    }

    // 2. Prestataire démarre le travail
    @PatchMapping("/{id}/demarrer")
    public CommandeDTO demarrerPrestation(@PathVariable Long id) {
        Commande c = commandeService.changerStatut(id, StatutCommande.EN_COURS);
        return mapper.toCommandeDTO(c);
    }

    // 3. Prestataire livre le fichier final
    @PostMapping("/{id}/livrer")
    public CommandeDTO livrerPrestation(@PathVariable Long id, @RequestParam String url) {
        Commande c = commandeService.livrerCommande(id, url);
        return mapper.toCommandeDTO(c);
    }

    // 4. Client valide la réception
    @PatchMapping("/{id}/valider")
    public CommandeDTO validerLivraison(@PathVariable Long id) {
        Commande c = commandeService.changerStatut(id, StatutCommande.TERMINE);
        return mapper.toCommandeDTO(c);
    }

    // --- LECTURE DES DONNÉES ---

    // Récupérer les commandes d'un Client (Mon Historique d'achat)
    @GetMapping("/client/{clientId}")
    public List<CommandeDTO> getCommandesByClient(@PathVariable Long clientId) {
        return commandeRepo.findByClientId(clientId).stream()
                .map(mapper::toCommandeDTO)
                .collect(Collectors.toList());
    }

    // Récupérer les commandes d'un Prestataire (Mes missions à faire)
    @GetMapping("/prestataire/{prestataireId}")
    public List<CommandeDTO> getCommandesByPrestataire(@PathVariable Long prestataireId) {
        return commandeRepo.findByPack_ServiceOffre_PrestataireId(prestataireId).stream()
                .map(mapper::toCommandeDTO)
                .collect(Collectors.toList());
    }
}
