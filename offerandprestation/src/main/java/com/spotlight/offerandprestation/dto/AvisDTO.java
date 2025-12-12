package com.spotlight.offerandprestation.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AvisDTO {
    private Long id;
    private int note;
    private String commentaire;
    private Long clientId;
    private LocalDateTime dateCreation;
}
