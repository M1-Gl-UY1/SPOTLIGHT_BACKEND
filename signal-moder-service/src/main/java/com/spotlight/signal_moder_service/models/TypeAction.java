package com.spotlight.signal_moder_service.models;




public enum TypeAction {
    // Actions sur les utilisateurs
    SUSPENSION_CLIENT,
    SUSPENSION_PRESTATAIRE,
    AVERTISSEMENT_CLIENT,
    AVERTISSEMENT_PRESTATAIRE,

    // Actions sur le catalogue
    ANNULATION_SERVICE,

    // Actions sur le contenu
    SUPPRESSION_COMMENTAIRE,
    SUPPRESSION_TCHAT_MESSAGE
}
