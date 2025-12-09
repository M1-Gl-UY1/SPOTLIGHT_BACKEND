package com.spotlight.signal_moder_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "chat-service")
public interface ChatServiceClient {
    // Gère SUPPRESSION_TCHAT_MESSAGE
    @DeleteMapping("/api/messages/{id}")
    void supprimerMessage(@PathVariable("id") Long id);
}