package com.spotlight.signal_moder_service.models;

import java.time.LocalDateTime;

import com.spotlight.signal_moder_service.enums.StatutStignalement;
import com.spotlight.signal_moder_service.enums.TypeCible;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "signalements")
public class Signalement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "L'ID de l'auteur est obligatoire")
    private Long auteurId;

    @NotNull(message = "L'ID de la cible est obligatoire")
    private Long cibleId;

    @NotNull(message = "Le type de cible est obligatoire")
    @Enumerated(EnumType.STRING)
    private TypeCible typeCible;

    @NotNull(message = "Le motif ne peut pas être nul")
    @Size(min = 5, max = 500, message = "Le motif doit contenir entre 5 et 500 caractères")
    private String motif;

    @Column(updatable = false)
    private LocalDateTime dateCreation;

    @Enumerated(EnumType.STRING)
    private StatutStignalement statut = StatutStignalement.EN_ATTENTE;

    // Relation bidirectionnelle (facultatif, mais pratique pour voir la décision depuis le signalement)
    @OneToOne(mappedBy = "signalement", cascade = CascadeType.ALL)
    private Moderation moderation;

    // S'exécute automatiquement avant d'enregistrer en base
    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = StatutStignalement.EN_ATTENTE;
        }
    }
}