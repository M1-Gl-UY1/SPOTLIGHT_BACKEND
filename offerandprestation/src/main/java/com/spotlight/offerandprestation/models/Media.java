package com.spotlight.offerandprestation.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;   // ex: http://localhost:8091/uploads/image123.jpg
    private String type;  // "IMAGE" ou "VIDEO"

    @ManyToOne
    @JoinColumn(name = "service_offre_id")
    @JsonIgnore // Important pour ne pas boucler en JSON
    private ServiceOffre serviceOffre;
}
