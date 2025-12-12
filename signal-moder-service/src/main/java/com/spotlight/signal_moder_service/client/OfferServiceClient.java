package com.spotlight.signal_moder_service.client;


import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@FeignClient(name = "OFFERS-SERVICE", url = "http://localhost:8081") // URL à adapter
public interface OfferServiceClient {

    // Appelle la route PATCH /api/v1/orders/{id}/status que nous avons faite tout à l'heure
    // Mais ici on triche un peu : l'admin force le statut
    @PostMapping("/api/v1/orders/{id}/arbitrage") // Route Admin cachée ou réutilisation du PATCH
    void arbitrerLitige(@PathVariable("id") Long commandeId, @RequestParam("status") String status);
    @PatchMapping("/api/v1/orders/{id}/status")
    void updateOrderStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> payload);
}