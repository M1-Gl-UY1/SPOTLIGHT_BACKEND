package com.spotlight.offerandprestation.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Avis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int note; // De 1 à 5
    
    @Column(length = 1000)
    private String commentaire;
    
    private LocalDateTime dateCreation;

    // Lien vers la commande (Preuve d'achat)
    @OneToOne
    @JoinColumn(name = "commande_id", unique = true) // unique=true empêche 2 avis sur 1 commande
    @JsonIgnore
    private Commande commande;

    // Lien vers le service (Pour faciliter les requêtes de moyenne)
    @ManyToOne
    @JoinColumn(name = "service_offre_id")
    @JsonIgnore
    private ServiceOffre serviceOffre;

    // ID du client (Redondant avec commande mais pratique pour l'affichage)
    private Long clientId;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
}
