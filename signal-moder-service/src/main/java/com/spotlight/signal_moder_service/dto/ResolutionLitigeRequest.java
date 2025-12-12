package com.spotlight.signal_moder_service.dto;



import lombok.Data;

@Data
public class ResolutionLitigeRequest {
    private String decision; // Ex: "REFUND_CLIENT", "RELEASE_PAYMENT"
    private String justification;
}