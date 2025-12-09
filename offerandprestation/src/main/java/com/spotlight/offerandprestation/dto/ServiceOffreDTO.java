package com.spotlight.offerandprestation.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ServiceOffreDTO {
    private Long id;
    private String titre;
    private String description;
    private String categorie;
    private Long prestataireId; // Important pour le bouton "Contacter"
    private LocalDateTime dateCreation;
    
    // On inclut les listes ici
    private List<PackDTO> packs;
    private List<MediaDTO> medias;
    private Double noteMoyenne; // ex: 4.5
    private Long nombreAvis; // ex: 120 avis
}