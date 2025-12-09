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

    private int note; 
    
    @Column(length = 1000)
    private String commentaire;
    
    private LocalDateTime dateCreation;

    
    @OneToOne
    @JoinColumn(name = "commande_id", unique = true) 
    @JsonIgnore
    private Commande commande;

    
    @ManyToOne
    @JoinColumn(name = "service_offre_id")
    @JsonIgnore
    private ServiceOffre serviceOffre;

    
    private Long clientId;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
}
