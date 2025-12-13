const asyncHandler = require("express-async-handler");
const { generateToken, generateAccessToken } = require("../utils/generate-token");
const { models: { User, Provider, ReportUser } } = require("../models"); // Assure-toi d'avoir Provider et ReportUser
const { Op } = require('sequelize');

// ==========================================
// 1. AUTHENTIFICATION
// ==========================================

// POST /api/v1/auth/register
const register = asyncHandler(async (req, res) => {
    const { firstName, lastName, email, phone, password, country } = req.body;

    const existing = await User.findOne({ where: { email } });
    if (existing) {
        return res.status(401).json({ message: "User already exists" });
    }

    const user = await User.create({
        firstName, lastName, email, phone, country, password, status: 'ACTIF'
    });

    const accessToken = generateAccessToken(res, user.id, user.role);
    const refreshToken = generateToken(res, user.id, user.role);

    // Stockage token (selon ta logique actuelle)
    let tokens = user.refreshToken || [];
    await user.update({ refreshToken: [...tokens, refreshToken] });

    return res.status(201).json({
        id: user.id,
        firstName: user.firstName,
        email: user.email,
        role: user.role,
        status: user.status,
        accessToken
    });
});

// POST /api/v1/auth/login
const auth = asyncHandler(async (req, res) => {
    const { email, password } = req.body;
    
    const user = await User.findOne({ where: { email } });

    if (!user || !(await user.matchPassword(password))) {
        return res.status(401).json({ message: "Invalid email or password" });
    }

    if (user.status === 'SUSPENDU' || user.status === 'BANNI') {
        return res.status(403).json({ message: `Compte ${user.status}. Contactez le support.` });
    }

    const accessToken = generateAccessToken(res, user.id, user.role);
    const refreshToken = generateToken(res, user.id, user.role);

    // Gestion des cookies/tokens existants (ta logique)
    await user.update({ refreshToken: [refreshToken] });

    res.cookie("jwt", refreshToken, {
        httpOnly: true,
        sameSite: "None",
        secure: true,
        maxAge: 24 * 60 * 60 * 1000
    });

    return res.status(200).json({
        id: user.id,
        firstName: user.firstName,
        role: user.role,
        accessToken
    });
});

// ==========================================
// 2. PROFILS & GESTION USER
// ==========================================

// GET /api/v1/users/:id/profile
const getUserProfile = asyncHandler(async (req, res) => {
    const { id } = req.params;
    
    // On inclut le profil prestataire si existe
    const includeProvider = Provider ? [{ model: Provider, as: 'providerProfile' }] : [];
    
    const user = await User.findByPk(id, {
        attributes: { exclude: ['password', 'refreshToken'] },
        include: includeProvider
    });

    if (!user) return res.status(404).json({ message: "User not found" });

    return res.status(200).json(user);
});

// PUT /api/v1/users/:id/profile
const updateUserProfile = asyncHandler(async (req, res) => {
    const { id } = req.params;
    
    // Vérification : seul l'utilisateur peut modifier son propre profil (sauf admin)
    // req.user est injecté par le middleware 'protect'
    if (req.user.id != id && req.user.role !== 'admin') {
        return res.status(403).json({ message: "Unauthorized" });
    }

    const user = await User.findByPk(id);
    if (!user) return res.status(404).json({ message: "User not found" });

    const { firstName, lastName, country, phone } = req.body;

    await user.update({
        firstName: firstName || user.firstName,
        lastName: lastName || user.lastName,
        country: country || user.country,
        phone: phone || user.phone
    });

    return res.status(200).json(user);
});

// POST /api/v1/users/providers (UPGRADE)
const upgradeToProvider = asyncHandler(async (req, res) => {
    const userId = req.user.id;
    const { certifications, nationalId, degrees, name } = req.body;

    const user = await User.findByPk(userId);
    if (!user) return res.status(404).json({ message: "User not found" });

    // Vérifier si déjà prestataire
    if (user.role === 'provider') {
        return res.status(400).json({ message: "User is already a provider" });
    }

    // 1. Update Role
    await user.update({ role: 'provider' });

    // 2. Create Provider Entry
    // Assure-toi que le modèle Provider est importé
    if (Provider) {
        await Provider.create({
            userId: user.id,
            name: name || `${user.firstName} ${user.lastName}`,
            nationalId,
            certifications,
            degrees,
            rating: 0
        });
    }

    return res.status(201).json({ message: "User upgraded to Provider successfully" });
});

// PUT /api/v1/users/:id/status (INTERNE MODERATION)
const updateUserStatus = asyncHandler(async (req, res) => {
    const { id } = req.params;
    const { status } = req.body; // 'SUSPENDU', 'ACTIF'

    // Ici pas de check req.user.id car c'est un appel admin/service
    const user = await User.findByPk(id);
    if (!user) return res.status(404).json({ message: "User not found" });

    await user.update({ status: status });

    return res.status(200).json({ message: `User status updated to ${status}` });
});

// ==========================================
// 3. SIGNALEMENTS (REPORTS)
// ==========================================

// POST /api/v1/reports
const createReport = asyncHandler(async (req, res) => {
    const reporterId = req.user.id;
    const { reportedUserId, reason, severity } = req.body;

    if (ReportUser) {
        const report = await ReportUser.create({
            reporterId,
            reportedUserId,
            reason,
            severity: severity || 'low',
            status: 'PENDING'
        });
        return res.status(201).json(report);
    } else {
        return res.status(500).json({ message: "Report Model missing" });
    }
});

// GET /api/v1/reports/:id (INTERNE MODERATION)
const getReport = asyncHandler(async (req, res) => {
    const { id } = req.params;

    if (ReportUser) {
        const report = await ReportUser.findByPk(id);
        if (!report) return res.status(404).json({ message: "Report not found" });
        return res.status(200).json(report);
    }
    return res.status(500).json({ message: "Report Model missing" });
});

module.exports = {
    register,
    auth,
    getUserProfile,
    updateUserProfile,
    upgradeToProvider,
    updateUserStatus,
    createReport,
    getReport
};