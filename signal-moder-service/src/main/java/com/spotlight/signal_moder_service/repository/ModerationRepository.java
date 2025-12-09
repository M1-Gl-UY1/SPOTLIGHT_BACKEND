package com.spotlight.signal_moder_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spotlight.signal_moder_service.models.Moderation;

@Repository
public interface ModerationRepository extends JpaRepository<Moderation, Long> {
    // On pourra ajouter des recherches par admin plus tard
}