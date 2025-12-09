package com.spotlight.offerandprestation.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom; // Ex: "Pack Standard"
    private String description;
    private Double prix;
    private int delaiJours;
    private int revisionsMax;

 
   @ManyToOne
    @JoinColumn(name = "service_offre_id")
    @JsonIgnore // Pour éviter la boucle infinie en JSON
    private ServiceOffre serviceOffre;
}