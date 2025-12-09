package com.spotlight.offerandprestation.dto;

import lombok.Data;

@Data
public class PackDTO {
    private Long id;
    private String nom;
    private String description;
    private Double prix;
    private int delaiJours;
    private int revisionsMax;
}