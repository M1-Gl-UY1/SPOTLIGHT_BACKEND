# Utiliser une image Python légère officielle
FROM python:3.11-slim

# Définir le dossier de travail dans le conteneur
WORKDIR /app

# Empêcher Python d'écrire des fichiers .pyc
ENV PYTHONDONTWRITEBYTECODE=1
# Afficher les logs Python directement dans le terminal
ENV PYTHONUNBUFFERED=1

# Installer les dépendances système nécessaires pour mysqlclient
RUN apt-get update && apt-get install -y \
    gcc \
    default-libmysqlclient-dev \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

# Copier le fichier des dépendances
COPY requirements.txt .

# Installer les dépendances Python
RUN pip install --no-cache-dir -r requirements.txt

# Copier tout le code du projet dans le conteneur
COPY . .

# Exposer le port (informatif)
EXPOSE 8000

# Commande par défaut (sera surchargée par docker-compose)
CMD ["python", "manage.py", "runserver", "0.0.0.0:8000"]