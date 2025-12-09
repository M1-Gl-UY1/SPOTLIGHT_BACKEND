package com.spotlight.offerandprestation.dto;

import java.time.LocalDateTime;

import com.spotlight.offerandprestation.enums.StatutCommande;

import lombok.Data;

@Data
public class CommandeDTO {
    private Long id;
    private Long clientId;
    private Long prestataireId; // Utile pour savoir qui fait le travail
    
    // Infos résumées du service acheté
    private String titreService;
    private String nomPack;
    private Double prixPaye;
    
    private StatutCommande statut;
    private LocalDateTime dateCommande;
    private LocalDateTime dateLivraison;
    private String livrableUrl;
}
