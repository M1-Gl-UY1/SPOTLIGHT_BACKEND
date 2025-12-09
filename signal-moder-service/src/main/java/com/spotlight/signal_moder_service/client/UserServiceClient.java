package com.spotlight.signal_moder_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    // Gère SUSPENSION_CLIENT et SUSPENSION_PRESTATAIRE
    // On passe le rôle en paramètre pour savoir qui on bloque
    @PutMapping("/api/users/{id}/statut")
    void changerStatutUtilisateur(@PathVariable("id") Long id, 
                                  @RequestParam("statut") String statut,
                                  @RequestParam("dateFin") String dateFin); 
}