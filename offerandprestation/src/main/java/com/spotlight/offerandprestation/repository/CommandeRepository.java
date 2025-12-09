package com.spotlight.offerandprestation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spotlight.offerandprestation.models.Commande;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {
    List<Commande> findByClientId(Long clientId);
    // Pour que le prestataire voie ses commandes, on devra faire une requête plus complexe 
    // ou filtrer, mais pour simplifier ici :
    List<Commande> findByPack_ServiceOffre_PrestataireId(Long prestataireId);
}