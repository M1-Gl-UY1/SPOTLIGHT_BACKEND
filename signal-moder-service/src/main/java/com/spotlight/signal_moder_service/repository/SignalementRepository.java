package com.spotlight.signal_moder_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spotlight.signal_moder_service.enums.StatutStignalement;
import com.spotlight.signal_moder_service.enums.TypeCible;
import com.spotlight.signal_moder_service.models.Signalement;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement, Long> {
    
    // Pour afficher la liste des signalements "EN_ATTENTE" à l'admin
    List<Signalement> findByStatut(StatutStignalement statut);

    // Pour voir l'historique des signalements sur une personne ou un service précis
    List<Signalement> findByCibleIdAndTypeCible(Long cibleId, TypeCible typeCible);
}