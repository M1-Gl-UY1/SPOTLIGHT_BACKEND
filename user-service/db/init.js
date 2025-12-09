const { models } = require('../models');
const bcrypt = require('bcrypt');
const { generateAccessToken, generateToken} = require('../utils/generate-token');

async function seedDatabase() {

    const count = await models.User.count();
    if (count > 0) {
        console.log("Skipping seeding — users already exist.");
        return;
    }

    const usersData = [];
    for (let i = 1; i <= 15; i++) {
        usersData.push({
            firstName: `UserFirst${i}`,
            lastName: `UserLast${i}`,
            email: `user${i}@example.com`,
            password: `password${i}`,
            phone: `9000000${i.toString().padStart(2, '0')}`,
            country: `Country${i}`,
            role: 'user',
        });
    }

    for (let i = 1; i <= 3; i++) {
        usersData.push({
            firstName: `AdminFirst${i}`,
            lastName: `AdminLast${i}`,
            email: `admin${i}@example.com`,
            password: `adminpass${i}`,
            phone: `9100000${i.toString().padStart(2, '0')}`,
            country: `AdminCountry${i}`,
            role: 'admin',
        });
    }

    const createdUsers = [];
    for (const u of usersData) {
        const user = await models.User.create(u);

        const refreshToken = generateToken(null, user.id, user.role);
        const accessToken = generateAccessToken(null, user.id, user.role)

        await user.update({refreshToken: [...user.refreshToken, refreshToken]});

        createdUsers.push(user);
    }

    const providersData = [];
    for (let i = 0; i < 14; i++) {
        const user = createdUsers[i];

        if (await models.Provider.findOne({ where: { userId: user.id } })) continue;

        providersData.push({
            name: `Provider${i + 1}`,
            certifications: `Certifications${i + 1}`,
            nationalId: `NID${1000 + i + 1}`,
            phone: `9500000${(i + 1).toString().padStart(2, '0')}`,
            email: `provider${i + 1}@example.com`,
            rating: Math.floor(Math.random() * 5),
            degrees: `Degree${i + 1}`,
            userId: user.id,
        });
    }

    for (const p of providersData) {
        await models.Provider.create(p);
    }

    const reportUsersData = [
        { reason: 'Spam', severity: 'medium', reporterId: createdUsers[0].id, reportedUserId: createdUsers[1].id },
        { reason: 'Abuse', severity: 'high', reporterId: createdUsers[2].id, reportedUserId: createdUsers[3].id },
        { reason: 'Harassment',  severity: 'critical', reporterId: createdUsers[4].id, reportedUserId: createdUsers[5].id },
        { reason: 'Inappropriate content', severity: 'low', reporterId: createdUsers[6].id, reportedUserId: createdUsers[7].id },
    ];

    for (const r of reportUsersData) {
        await models.ReportUser.create(r);
    }
}

module.exports = { seedDatabase };
