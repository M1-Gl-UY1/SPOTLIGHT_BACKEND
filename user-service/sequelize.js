const { Sequelize } = require("sequelize");

const dbName = process.env.PG_DB || "talent";
const dbUser = process.env.PG_USER || "postgres";
const dbPass = process.env.PG_PASSWORD || "password";
const dbHost = process.env.PG_HOST || "localhost";
const dbPort = process.env.PG_PORT || 5432;

const connectionString = `postgres://${dbUser}:${dbPass}@${dbHost}:${dbPort}/${dbName}`;

//Initialisation de Sequelize avec l'URI

const sequelize = new Sequelize(connectionString, {
  dialect: "postgres",
  logging: false, // Garder logging false pour un environnement propre

});

module.exports = sequelize;