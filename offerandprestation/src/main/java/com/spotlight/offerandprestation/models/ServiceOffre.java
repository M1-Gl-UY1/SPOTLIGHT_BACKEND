package com.spotlight.offerandprestation.models;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data 
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOffre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    
    @Column(length = 2000) // Texte long
    private String description;

    private String categorie;
    
    // ID du prestataire (Référence vers le Microservice Utilisateur)
    private Long prestataireId; 
    
    private LocalDateTime dateCreation;
    private boolean actif;

    // Un Service a plusieurs Packs (Basic, Premium...)
    @OneToMany(mappedBy = "serviceOffre", cascade = CascadeType.ALL)
    private List<Pack> packs;

    @OneToMany(mappedBy = "serviceOffre", cascade = CascadeType.ALL)
    private List<Media> medias;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        actif = true;
    }
}