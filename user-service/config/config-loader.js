const axios = require('axios');

// En local on tape sur localhost. Dans Docker, ce sera le nom du service.
// On utilise une variable d'env pour l'URL, avec localhost par défaut.
const CONFIG_SERVER_URL = process.env.CONFIG_SERVER_URL || 'http://localhost:8888/user-service/default';

async function loadConfig() {
    try {
        console.log(`🔌 Tentative de connexion au Config Server : ${CONFIG_SERVER_URL}`);
        const response = await axios.get(CONFIG_SERVER_URL);
        
        // Le Config Server renvoie la config. On l'injecte dans process.env
        if (response.data && response.data.propertySources) {
            const properties = response.data.propertySources[0].source;
            console.log("📥 Configuration reçue :");
            for (const [key, value] of Object.entries(properties)) {
                process.env[key] = value;
                // On affiche les clés chargées (pas les mots de passe pour la sécurité)
                if(!key.includes('PASSWORD') && !key.includes('SECRET')) {
                    console.log(`   -> ${key} = ${value}`);
                }
            }
            console.log("✅ Configuration chargée dans process.env");
        }
    } catch (error) {
        console.warn("⚠️ Impossible de joindre le Config Server (C'est normal si tu lances le Dockerfile test_user seul).");
        console.warn("   -> Utilisation des variables d'environnement locales ou du fichier .env");
        console.error("   -> Erreur :", error.message);
    }
}

module.exports = loadConfig;