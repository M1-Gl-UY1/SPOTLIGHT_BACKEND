package com.spotlight.signal_moder_service.dto;

import java.time.LocalDateTime;

import com.spotlight.signal_moder_service.models.TypeAction;

import lombok.Data;

@Data
public class ModerationRequest {
    private Long adminId;
    private Long signalementId;
    private TypeAction action;
    private String justification;
    private LocalDateTime dateFinSuspension; // Optionnel (peut être null)
}
