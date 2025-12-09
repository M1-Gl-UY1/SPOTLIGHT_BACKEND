
// db.js — Checks and creates the database if missing
const { Sequelize } = require("sequelize");

const createDatabase = async () => {
  try {
    const dbName = process.env.PG_DB;
    const dbUser = process.env.PG_USER;
    const dbPass = process.env.PG_PASSWORD;
    const dbHost = process.env.PG_HOST || "localhost";
    const dbPort = process.env.PG_PORT || 5432;

    // Connect to default postgres database
    const root = new Sequelize("postgres", dbUser, dbPass, {
      host: dbHost,
      port: dbPort,
      dialect: "postgres",
      logging: false,
    });

    await root.authenticate();

    // Check for database
    const [exists] = await root.query(
      `SELECT 1 FROM pg_database WHERE datname = '${dbName}';`
    );

    if (exists.length === 0) {
      console.log(`Database "${dbName}" does NOT exist — creating...`);
      await root.query(`CREATE DATABASE "${dbName}";`);
      console.log(`Database "${dbName}" created successfully.`);
    } else {
      console.log(`Database "${dbName}" already exists.`);
    }

    await root.close();
  } catch (err) {
    console.error("❌ Error ensuring DB exists");
    console.error(err);
    process.exit(1);
  }
};

module.exports = { createDatabase };
