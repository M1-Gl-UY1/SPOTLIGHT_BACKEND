package com.spotlight.offerandprestation.dto;

import lombok.Data;

@Data
public class MediaDTO {
    private Long id;
    private String url;
    private String type; // IMAGE ou VIDEO
}