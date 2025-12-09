package com.spotlight.offerandprestation.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spotlight.offerandprestation.enums.StatutCommande;
import com.spotlight.offerandprestation.models.Commande;
import com.spotlight.offerandprestation.models.Pack;
import com.spotlight.offerandprestation.repository.CommandeRepository;
import com.spotlight.offerandprestation.repository.PackRepository;

@Service
public class CommandeService {
    @Autowired
    private CommandeRepository commandeRepo;
    @Autowired
    private PackRepository packRepo;

    public Commande passerCommande(Long clientId, Long packId) {
        Pack pack = packRepo.findById(packId)
            .orElseThrow(() -> new RuntimeException("Pack introuvable"));
            
        Commande commande = new Commande();
        commande.setClientId(clientId);
        commande.setPack(pack);
        // Le statut EN_ATTENTE est mis par @PrePersist
        return commandeRepo.save(commande);
    }

    public Commande changerStatut(Long commandeId, StatutCommande nouveauStatut) {
        Commande c = commandeRepo.findById(commandeId)
            .orElseThrow(() -> new RuntimeException("Commande introuvable"));
        
        c.setStatut(nouveauStatut);
        
        if (nouveauStatut == StatutCommande.LIVRE) {
            c.setDateLivraison(LocalDateTime.now());
        }
        
        return commandeRepo.save(c);
    }

    public Commande livrerCommande(Long commandeId, String urlLivrable) {
        Commande c = commandeRepo.findById(commandeId)
            .orElseThrow(() -> new RuntimeException("Commande introuvable"));
            
        c.setLivrableUrl(urlLivrable);
        c.setStatut(StatutCommande.LIVRE);
        c.setDateLivraison(LocalDateTime.now());
        return commandeRepo.save(c);
    }
}
