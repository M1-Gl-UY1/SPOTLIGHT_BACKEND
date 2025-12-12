package com.spotlight.offerandprestation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.spotlight.offerandprestation.models.Avis;

import java.util.List;

public interface AvisRepository extends JpaRepository<Avis, Long> {
    
    // Trouver tous les avis d'un service spécifique
    List<Avis> findByServiceOffreId(Long serviceId);

    // Calculer la moyenne des notes pour un service
    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.serviceOffre.id = :serviceId")
    Double getMoyenneNoteByServiceId(@Param("serviceId") Long serviceId);
    
    // Compter combien d'avis a ce service
    Long countByServiceOffreId(Long serviceId);

    @Query("SELECT AVG(a.note) FROM Avis a " +
           "JOIN a.serviceOffre s " +
           "WHERE s.prestataireId = :prestataireId")
    Double getMoyenneGlobalePrestataire(@Param("prestataireId") Long prestataireId);
    
    @Query("SELECT COUNT(a) FROM Avis a " +
           "JOIN a.serviceOffre s " +
           "WHERE s.prestataireId = :prestataireId")
    Long countAvisGlobauxPrestataire(@Param("prestataireId") Long prestataireId);
}
