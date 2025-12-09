package com.spotlight.offerandprestation.dto;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.spotlight.offerandprestation.models.Commande;
import com.spotlight.offerandprestation.models.Media;
import com.spotlight.offerandprestation.models.Pack;
import com.spotlight.offerandprestation.models.ServiceOffre;

@Component
public class DtoMapper {

    // Conversion Service -> DTO
    public ServiceOffreDTO toServiceDTO(ServiceOffre service) {
        ServiceOffreDTO dto = new ServiceOffreDTO();
        dto.setId(service.getId());
        dto.setTitre(service.getTitre());
        dto.setDescription(service.getDescription());
        dto.setCategorie(service.getCategorie());
        dto.setPrestataireId(service.getPrestataireId());
        dto.setDateCreation(service.getDateCreation());

        // Conversion des listes (Packs et Medias)
        if (service.getPacks() != null) {
            dto.setPacks(service.getPacks().stream()
                    .map(this::toPackDTO)
                    .collect(Collectors.toList()));
        }
        
        if (service.getMedias() != null) {
            dto.setMedias(service.getMedias().stream()
                    .map(this::toMediaDTO)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }

    public PackDTO toPackDTO(Pack pack) {
        PackDTO dto = new PackDTO();
        dto.setId(pack.getId());
        dto.setNom(pack.getNom());
        dto.setDescription(pack.getDescription());
        dto.setPrix(pack.getPrix());
        dto.setDelaiJours(pack.getDelaiJours());
        dto.setRevisionsMax(pack.getRevisionsMax());
        return dto;
    }

    public MediaDTO toMediaDTO(Media media) {
        MediaDTO dto = new MediaDTO();
        dto.setId(media.getId());
        dto.setUrl(media.getUrl());
        dto.setType(media.getType());
        return dto;
    }

    // Conversion Commande -> DTO
    public CommandeDTO toCommandeDTO(Commande commande) {
        CommandeDTO dto = new CommandeDTO();
        dto.setId(commande.getId());
        dto.setClientId(commande.getClientId());
        dto.setStatut(commande.getStatut());
        dto.setDateCommande(commande.getDateCommande());
        dto.setDateLivraison(commande.getDateLivraison());
        dto.setLivrableUrl(commande.getLivrableUrl());

        // Récupération des infos liées au Pack et Service
        if (commande.getPack() != null) {
            dto.setNomPack(commande.getPack().getNom());
            dto.setPrixPaye(commande.getPack().getPrix());
            
            if (commande.getPack().getServiceOffre() != null) {
                dto.setTitreService(commande.getPack().getServiceOffre().getTitre());
                dto.setPrestataireId(commande.getPack().getServiceOffre().getPrestataireId());
            }
        }
        return dto;
    }
}