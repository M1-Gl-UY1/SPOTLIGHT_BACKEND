package com.spotlight.signal_moder_service.models;

import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "moderations")
public class Moderation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "L'ID de l'administrateur est requis")
    private Long adminId;

    // Grâce à ce lien, on sait déjà qui est la Cible (via le signalement)
    @OneToOne
    @JoinColumn(name = "signalement_id", unique = true)
    @NotNull(message = "Une modération doit être liée à un signalement")
    private Signalement signalement;

    // --- CORRECTION : On garde seulement TypeAction ---
    @NotNull(message = "Le type d'action est requis")
    @Enumerated(EnumType.STRING)
    private TypeAction typeAction; 
    // --------------------------------------------------

    @NotBlank(message = "La justification est obligatoire")
    @Column(columnDefinition = "TEXT")
    private String justification;

    private LocalDateTime dateDecision;

    @Future(message = "La date de fin de suspension doit être dans le futur")
    private LocalDateTime dateFinSuspension;

    @PrePersist
    protected void onCreate() {
        this.dateDecision = LocalDateTime.now();
    }
}