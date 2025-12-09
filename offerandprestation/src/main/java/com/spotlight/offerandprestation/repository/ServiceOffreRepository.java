package com.spotlight.offerandprestation.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.spotlight.offerandprestation.models.ServiceOffre;

import java.util.List;

public interface ServiceOffreRepository extends JpaRepository<ServiceOffre, Long> {
    List<ServiceOffre> findByPrestataireId(Long prestataireId);
    List<ServiceOffre> findByCategorie(String categorie);
}