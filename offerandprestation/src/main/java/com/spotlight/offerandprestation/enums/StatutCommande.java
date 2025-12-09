package com.spotlight.offerandprestation.enums;
public enum StatutCommande {
    EN_ATTENTE,   // Commandé mais pas commencé
    EN_COURS,     // Prestataire a commencé
    LIVRE,        // Prestataire a livré, attente validation client
    TERMINE,      // Validé par le client (Argent débloqué)
    LITIGE,       // Problème signalé
    ANNULE
}
