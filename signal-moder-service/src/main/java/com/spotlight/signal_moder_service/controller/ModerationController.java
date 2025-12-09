package com.spotlight.signal_moder_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spotlight.signal_moder_service.dto.ModerationRequest;
import com.spotlight.signal_moder_service.models.Moderation;
import com.spotlight.signal_moder_service.service.ModerationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;

    // ACTION ADMIN : Appliquer une sanction ou une décision
    @PostMapping("/appliquer")
    public ResponseEntity<Moderation> appliquerDecision(@RequestBody ModerationRequest request) {
        
        Moderation moderation = moderationService.appliquerDecision(
                request.getAdminId(),
                request.getSignalementId(),
                request.getAction(),
                request.getJustification(),
                request.getDateFinSuspension()
        );

        return ResponseEntity.ok(moderation);
    }
}
