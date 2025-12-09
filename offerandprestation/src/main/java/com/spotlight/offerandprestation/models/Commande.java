package com.spotlight.offerandprestation.models;

import java.time.LocalDateTime;

import com.spotlight.offerandprestation.enums.StatutCommande;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID du client (Référence vers Microservice Utilisateur)
    private Long clientId;

    // Quel pack a été acheté ?
    @ManyToOne
    @JoinColumn(name = "pack_id")
    private Pack pack;

    @Enumerated(EnumType.STRING)
    private StatutCommande statut;

    private LocalDateTime dateCommande;
    private LocalDateTime dateLivraison;
    
    // URL vers les fichiers livrés (S3, Cloudinary...)
    private String livrableUrl;

    @PrePersist
    protected void onCreate() {
        dateCommande = LocalDateTime.now();
        statut = StatutCommande.EN_ATTENTE;
    }
}