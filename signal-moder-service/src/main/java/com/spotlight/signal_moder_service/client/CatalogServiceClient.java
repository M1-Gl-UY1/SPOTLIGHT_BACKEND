package com.spotlight.signal_moder_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service")
public interface CatalogServiceClient {
    // Gère ANNULATION_SERVICE (Désactiver une offre)
    @DeleteMapping("/api/services/{id}")
    void supprimerOuMasquerService(@PathVariable("id") Long id);
}