package com.spotlight.offerandprestation.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spotlight.offerandprestation.enums.StatutCommande;
import com.spotlight.offerandprestation.models.Avis;
import com.spotlight.offerandprestation.models.Commande;
import com.spotlight.offerandprestation.repository.AvisRepository;
import com.spotlight.offerandprestation.repository.CommandeRepository;

@Service
public class AvisService {

    @Autowired
    private AvisRepository avisRepository;
    @Autowired
    private CommandeRepository commandeRepository;

    public Avis ajouterAvis(Long commandeId, int note, String commentaire) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        // Règle 1 : La commande doit être terminée
        if (commande.getStatut() != StatutCommande.TERMINE) {
            throw new RuntimeException("Vous ne pouvez noter qu'une commande terminée.");
        }

        // Règle 2 : Vérifier si un avis existe déjà (géré par database unique constraint, mais on peut double check ici)
        // (Optionnel si tu gères l'exception SQL)

        if (note < 1 || note > 5) {
            throw new RuntimeException("La note doit être entre 1 et 5.");
        }

        Avis avis = new Avis();
        avis.setNote(note);
        avis.setCommentaire(commentaire);
        avis.setCommande(commande);
        avis.setServiceOffre(commande.getPack().getServiceOffre()); // On remonte au Service
        avis.setClientId(commande.getClientId());

        return avisRepository.save(avis);
    }

    public List<Avis> getAvisByService(Long serviceId) {
        return avisRepository.findByServiceOffreId(serviceId);
    }

    public Double getMoyenne(Long serviceId) {
        Double moy = avisRepository.getMoyenneNoteByServiceId(serviceId);
        return (moy == null) ? 0.0 : moy;
    }
    
    public Long getNombreAvis(Long serviceId) {
        return avisRepository.countByServiceOffreId(serviceId);
    }
}