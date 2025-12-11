// On garde dotenv en fallback local si besoin, mais le Config Server passera par dessus
require('dotenv').config();

const loadConfig = require('./config/config-loader'); // <--- Ton loader créé juste avant
const Eureka = require('eureka-js-client').Eureka;    // <--- Client Eureka
const express = require('express');
const { createServer } = require('http');
const { Server } = require('socket.io');
const cookieParser = require('cookie-parser');
const cookieSession = require('cookie-session');
const passport = require('passport');
const cors = require('cors');


// ============================================================
// 1. CHARGEMENT DE LA CONFIGURATION (Config Server)
// ============================================================
loadConfig().then(async () => {

  // ============================================================
  // 2. IMPORT DES MODULES DEPENDANTS DE LA CONFIG (DB, ETC.)
  // ============================================================
  // On ne les importe que maintenant, car process.env est enfin rempli !
    const connectDB = require('./config/db');
    const db = require('./config/db');
    const sequelize = require('./sequelize');
    const { createAdmin, seedDatabase } = require('./db/init');

    const connectWithRetry = async (maxRetries = 10, delayMs = 3000) => {
        for (let i = 0; i < maxRetries; i++) {
            try {
                // L'étape critique
                await db.createDatabase();
                await sequelize.authenticate(); // Teste la connexion
                console.log("Database connection successful.");
                return true;
            } catch (err) {
                if (i < maxRetries - 1) {
                    console.warn(`Connection refused. Retrying in ${delayMs / 1000}s... (${i + 1}/${maxRetries})`);
                    await new Promise(resolve => setTimeout(resolve, delayMs));
                } else {
                    throw err; // Échec après toutes les tentatives
                }
            }
        }
    };

  // Initialisation de l'App
  const app = express();
  
  // Middleware
  app.use(cookieParser());
  app.use(cookieSession({
    name: 'session',
    keys: ['mimche'],
    maxAge: 24 * 60 * 60 * 1000
  }));

  app.use(passport.initialize());
  app.use(passport.session());

  const credentials = require("./middlewares/credentials");
  app.use(credentials);

  const corsOptions = {
    credentials: true,
    origin: 'https://mimlyricstest5.onrender.com',
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH']
  };
  app.use(cors(corsOptions));

  app.use("/public", express.static("public"));
  app.use(express.urlencoded({ extended: false }));
  app.use(express.json());

  // ============================================================
  // 3. INITIALISATION BASE DE DONNÉES
  // ============================================================
  try {
      await connectWithRetry(); // Attente et tentatives jusqu'à succès

      // Maintenant que nous sommes connectés, on peut synchroniser
      await sequelize.sync({ force: false });
      await seedDatabase();
      console.log("All models synced and DB seeded");
    } catch (err) {
      console.error("Database initialization failed after all retries:", err);
      process.exit(1); // Arrête le service si la DB est inaccessible
    }

  // Passport config
  require('./utils/passport-google');

  // Routes
  app.use('/api/v1', require("./routes/userRoutes"));
  app.use('/api/v1', require("./routes/refreshRoutes"));
  app.use('/api/v1', require("./routes/authRoutes"));
  app.use('/api/v1/providers', require('./routes/providerRoutes'));
  app.use('/api/v1/report-users', require('./routes/ReportUserRoutes'));

  // Error middlewares
  const { notFound, errorHandler } = require("./middlewares/errorMiddleware");
  app.use(notFound);
  app.use(errorHandler);

  // ============================================================
  // 4. SOCKET.IO SETUP
  // ============================================================
  const httpServer = createServer(app);
  const io = new Server(httpServer, {
    cors: {
      credentials: true,
      origin: function(origin, callback) {
        const allowedOrigins = ['*'];
        if (!origin || allowedOrigins.indexOf(origin) !== -1) callback(null, true);
        else callback(new Error('Not allowed by CORS'));
      },
      methods: ["GET", "POST", "PUT", "DELETE"],
    }
  });

  let users = [];
  const addUser = ({ id, phone, room, avatar, username }) => {
    const existingUser = users.find(u => u.room === room && u.phone === phone);
    if (existingUser) return { error: 'Username is taken' };
    const user = { id, phone, room, avatar, username };
    users.push(user);
    return { user };
  };
  const removeUser = (id) => users = users.filter(user => user.id !== id);
  const getUser = (id) => users.find(user => user.id === id);
  const getUsersInRoom = (room) => users.filter(user => user.room === room);

  io.on("connection", (socket) => {
    socket.on('join', ({ phone, room, avatar, username }, callback) => {
      const id = socket.id;
      const { error, user } = addUser({ id, phone, room, avatar, username });
      if (error) return callback(error); // Fix: handle error properly
      
      socket.join(room); // Join first
      io.emit("getUser", { users, user });
      
      socket.emit('message', { user: 'admin', text: `Welcome ${phone} !` });
      socket.broadcast.to(room).emit('message', { user: 'admin', text: `${username} has joined` });
      
      callback();
    });

    socket.on("sendMessage", ({ from, text, avatar, username }, callback) => {
      const user = getUser(socket.id);
      if (user) io.to(user.room).emit('message', { user: from, text, avatar, username });
      callback();
    });

    socket.on("disconnect", () => {
      removeUser(socket.id);
      io.emit("getUsers", users);
    });
  });

  // ============================================================
  // 5. DEMARRAGE SERVEUR + EUREKA
  // ============================================================
  const port = process.env.PORT || 5175;
  
  httpServer.listen(port, () => {
    console.log(`🚀 Server running on port ${port}`);

    // --- ENREGISTREMENT EUREKA DYNAMIQUE ---
    
    // Dans Docker, le hostname est souvent l'ID du container ou le nom du service
    const hostName = process.env.EUREKA_INSTANCE_HOSTNAME || 'regisrty-service';
    const ipAddr = process.env.EUREKA_INSTANCE_IP_ADDRESS || '127.0.0.1';

    const client = new Eureka({
      instance: {
        app: 'USER-SERVICE',
        hostName: hostName,
        ipAddr: ipAddr,
        statusPageUrl: `http://${hostName}:${port}/info`,
        healthCheckUrl: `http://${hostName}:${port}/health`, // Assure-toi d'avoir une route /health
        port: {
          '$': port,
          '@enabled': 'true',
        },
        vipAddress: 'user-service',
        dataCenterInfo: {
          '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
          name: 'MyOwn',
        },
      },
      eureka: {
        host: process.env.EUREKA_HOST || 'registry-service',
        port: process.env.EUREKA_PORT || 8761,
        servicePath: '/eureka/apps/'
      },
    });

    client.start((error) => {
      console.log(error || ' User Service registered to Eureka');
    });
  });

}).catch(err => {
  console.error(" CRITICAL ERROR: Could not load configuration or start server", err);
  process.exit(1);
});