# Spotlight – Backend Microservices Architecture

Backend de la plateforme **Spotlight**, implémenté selon une architecture microservices polyglotte (Spring Boot, Node.js, Python Django). Le système est conçu pour la scalabilité, la résilience et une séparation claire des responsabilités.

---

![alt text](https://img.shields.io/badge/version-1.0.0-blue.svg)

## Dépôts Git

* **Dépôt principal (microservices, Docker, Kubernetes)**
  [https://github.com/TP-INF4057-SOFTWARE-ARCHITECTURE-2026/INF4057-TP-SoftwareArchitecture-Groupe-1.git](https://github.com/TP-INF4057-SOFTWARE-ARCHITECTURE-2026/INF4057-TP-SoftwareArchitecture-Groupe-1.git)

* **Dépôt de configuration distante (Spring Cloud Config)**
  [https://github.com/M1-Gl-UY1/config-repo-spotlight.git](https://github.com/M1-Gl-UY1/config-repo-spotlight.git)

---

## Architecture technique

L’architecture repose sur le pattern **API Gateway** avec **Spring Cloud**, un **serveur de configuration centralisé** et un **registre de services**.

### Infrastructure et services cœur

| Service          | Technologie          | Port | Rôle                                        |
| ---------------- | -------------------- | ---- | ------------------------------------------- |
| Proxy Service    | Spring Cloud Gateway | 8080 | Point d’entrée unique, routage des requêtes |
| Registry Service | Netflix Eureka       | 8761 | Découverte des services                     |
| Config Service   | Spring Cloud Config  | 8888 | Configuration centralisée (Git-backed)      |
| Message Broker   | RabbitMQ             | 5672 | Communication asynchrone                    |

### Microservices métiers

| Service           | Stack             | Base de données | Description                      |
| ----------------- | ----------------- | --------------- | -------------------------------- |
| User Service      | Node.js / Express | PostgreSQL      | Utilisateurs et authentification |
| Chat Service      | Spring Boot       | MongoDB         | Messagerie temps réel            |
| Offer Service     | Spring Boot       | MySQL           | Offres et prestations            |
| Signal Service    | Spring Boot       | MySQL           | Signalement et modération        |
| Catalogue Service | Python / Django   | MySQL           | Catalogue de présentation        |

---

## Prérequis

* Docker et Docker Compose (v2+)
* Minimum 6 Go de RAM alloués à Docker
* Ports disponibles : 8080, 8761, 8888, 5432, 3306, 27017

---

## Installation et démarrage (Docker Compose)

### 1. Cloner le projet

```bash
git clone https://github.com/TP-INF4057-SOFTWARE-ARCHITECTURE-2026/INF4057-TP-SoftwareArchitecture-Groupe-1.git
cd INF4057-TP-SoftwareArchitecture-Groupe-1
```

### 2. Lancer la stack

```bash
docker compose up -d --build
```

Les bases de données sont initialisées automatiquement au premier lancement.

### 3. Suivi des logs

```bash
docker compose logs -f
```

### 4. Accès aux services

* API Gateway : [http://localhost:8080](http://localhost:8080)
* Eureka Dashboard : [http://localhost:8761](http://localhost:8761)
* RabbitMQ Management : [http://localhost:15672](http://localhost:15672) (guest / guest)

---

## Déploiement Kubernetes

Chaque microservice dispose de son propre dossier **k8s/** contenant ses manifestes Kubernetes (Deployment, Service, ConfigMap, Secret).

### Organisation Kubernetes

```bash
.
├── config-service/
│   └── k8s/
├── registry-service/
│   └── k8s/
├── proxy-service/
│   └── k8s/
├── user-service/
│   └── k8s/
├── chat/
│   └── k8s/
├── offerandprestation/
│   └── k8s/
├── signal-moder-service/
│   └── k8s/
├── spotlight_catalogue/
│   └── k8s/
├── spotlight-cluster.yaml
└── docker-compose.yml
```

### Déploiement du cluster (AWS EKS)

```bash
eksctl create cluster -f spotlight-cluster.yaml
```

### Déploiement des services

Ordre recommandé :

1. config-service
2. registry-service
3. bases de données et broker
4. services métiers
5. proxy-service

```bash
kubectl apply -f config-service/k8s
kubectl apply -f registry-service/k8s
kubectl apply -f user-service/k8s
kubectl apply -f chat/k8s
kubectl apply -f offerandprestation/k8s
kubectl apply -f signal-moder-service/k8s
kubectl apply -f spotlight_catalogue/k8s
kubectl apply -f proxy-service/k8s
```

---

## Structure du projet

```bash
.
├── config-service/
├── registry-service/
├── proxy-service/
├── user-service/
├── chat/
├── offerandprestation/
├── signal-moder-service/
├── spotlight_catalogue/
├── uploads/
├── docker-compose.yml
└── init-postgres.sh
```

---

## Aide

**Connexion refusée au démarrage**
Les services démarrent de manière progressive. Attendre 1 à 2 minutes lors d’un premier lancement.

**Arrêt des conteneurs Java (Code 137)**
Mémoire insuffisante. Augmenter la RAM allouée à Docker ou désactiver certains services.

**Relance manuel des services**
une relance manuel pourrait être necessaire pour certains service.
---

## Auteurs et licence

Projet académique – `Master 1 Génie Logiciel, Université de Yaoundé I`.
Licence MIT.