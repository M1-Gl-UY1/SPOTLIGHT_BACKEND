const router = require("express").Router();
const { protect } = require("../middlewares/authMiddleware"); // Assure-toi que ce middleware existe
const {
    register,
    auth,
    getUserProfile,
    updateUserProfile,
    upgradeToProvider,
    updateUserStatus,
    createReport,
    getReport
} = require("../controllers/usercontroller");

// ==========================================
// 1. AUTHENTIFICATION
// ==========================================
router.post("/api/v1/auth/register", register);
router.post("/api/v1/auth/login", auth);

// ==========================================
// 2. GESTION DES UTILISATEURS & PROFILS
// ==========================================

// Récupérer le profil (Public ou propre profil)
router.get("/api/v1/users/:id/profile", getUserProfile);

// Mise à jour du profil (Nécessite d'être connecté)
router.put("/api/v1/users/:id/profile", protect, updateUserProfile);

// Upgrade Utilisateur -> Prestataire
router.post("/api/v1/users/providers", protect, upgradeToProvider);

// Sanction : Changer le statut (Réservé au service MODERATION ou Admin)
// Idéalement, créer un middleware 'protectAdmin' ou 'protectService'
router.put("/api/v1/users/:id/status", updateUserStatus);

// ==========================================
// 3. SIGNALEMENTS
// ==========================================

// Créer un signalement (Client/Prestataire connecté)
router.post("/api/v1/reports", protect, createReport);

// Lire un signalement (Service MODERATION)
router.get("/api/v1/reports/:id", getReport);

module.exports = router;