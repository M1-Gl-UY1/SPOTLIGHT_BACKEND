package com.spotlight.signal_moder_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "review-service")
public interface ReviewServiceClient {
    // Gère SUPPRESSION_COMMENTAIRE
    @DeleteMapping("/api/reviews/{id}")
    void supprimerCommentaire(@PathVariable("id") Long id);
}