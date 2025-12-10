FROM node:22-alpine

# 1. Installer Curl (Indispensable pour le Healthcheck du docker-compose)
RUN apk --no-cache add curl

WORKDIR /app

COPY package*.json ./

# 2. Installation propre (ci est mieux que install pour les builds, mais install va bien aussi)
RUN npm install

COPY . .

# 3. Port : Assure-toi que cela correspond à ton fichier properties (5000 ou 5175)
# Dans ton code server.js, tu as mis process.env.PORT || 5175.
# Dans le docker-compose précédent, c'était 5000. 
# Mettons 5000 pour standardiser.
EXPOSE 5000

# 4. Lancement direct (évite les problèmes de signaux d'arrêt avec npm)
CMD ["node", "server.js"]