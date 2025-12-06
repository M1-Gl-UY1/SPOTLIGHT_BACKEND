from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import CategorieViewSet, TagViewSet, PrestationViewSet, AvisViewSet, UserViewSet

# Création du router
router = DefaultRouter()

# Enregistrement des ViewSets
router.register(r'categories', CategorieViewSet, basename='categorie')
router.register(r'tags', TagViewSet, basename='tag')
router.register(r'prestations', PrestationViewSet, basename='prestation')
router.register(r'avis', AvisViewSet, basename='avis')
router.register(r'users', UserViewSet, basename='user')


# URLs de l'application
urlpatterns = [
    path('', include(router.urls)),
]


"""
📋 ROUTES GÉNÉRÉES AUTOMATIQUEMENT :

╔══════════════════════════════════════════════════════════════════════════╗
║                            CATÉGORIES                                     ║
╚══════════════════════════════════════════════════════════════════════════╝

GET     /categories/                    → Liste toutes les catégories
GET     /categories/{id}/               → Détail d'une catégorie
GET     /categories/{id}/prestations/   → Prestations d'une catégorie


╔══════════════════════════════════════════════════════════════════════════╗
║                              TAGS                                         ║
╚══════════════════════════════════════════════════════════════════════════╝

GET     /tags/                          → Liste tous les tags
GET     /tags/{id}/                     → Détail d'un tag


╔══════════════════════════════════════════════════════════════════════════╗
║                           PRESTATIONS                                     ║
╚══════════════════════════════════════════════════════════════════════════╝

GET     /prestations/                   → Liste des prestations (avec filtres)
GET     /prestations/{id}/              → Détail d'une prestation
POST    /prestations/                   → Créer une prestation (authentifié)
PUT     /prestations/{id}/              → Modifier une prestation (authentifié)
PATCH   /prestations/{id}/              → Modifier partiellement (authentifié)
DELETE  /prestations/{id}/              → Supprimer une prestation (authentifié)

--- ROUTES SPÉCIALES ---
GET     /prestations/populaires/        → Top 10 prestations populaires
GET     /prestations/meilleures_notes/  → Top 10 meilleures notes
GET     /prestations/nouveautes/        → 10 prestations les plus récentes
GET     /prestations/{id}/recommandations/  → Prestations similaires
POST    /prestations/{id}/incrementer_popularite/  → +1 popularité


╔══════════════════════════════════════════════════════════════════════════╗
║                              AVIS                                         ║
╚══════════════════════════════════════════════════════════════════════════╝

GET     /avis/                          → Liste des avis
GET     /avis/{id}/                     → Détail d'un avis
POST    /avis/                          → Créer un avis (authentifié)
PUT     /avis/{id}/                     → Modifier son avis (authentifié)
PATCH   /avis/{id}/                     → Modifier partiellement (authentifié)
DELETE  /avis/{id}/                     → Supprimer son avis (authentifié)


╔══════════════════════════════════════════════════════════════════════════╗
║                    EXEMPLES DE FILTRES & RECHERCHE                        ║
╚══════════════════════════════════════════════════════════════════════════╝

📌 Filtrer par catégorie :
GET /prestations/?categorie=1

📌 Filtrer par prix :
GET /prestations/?prix_min=20&prix_max=100

📌 Filtrer par tags (plusieurs) :
GET /prestations/?tags=1,2,3

📌 Filtrer par note minimale :
GET /prestations/?note_min=4

📌 Recherche par mot-clé :
GET /prestations/?search=réparation

📌 Trier les résultats :
GET /prestations/?ordering=prix              (prix croissant)
GET /prestations/?ordering=-prix             (prix décroissant)
GET /prestations/?ordering=-popularite       (plus populaires d'abord)
GET /prestations/?ordering=-note_moyenne     (meilleures notes d'abord)
GET /prestations/?ordering=-date_creation    (plus récentes d'abord)

📌 Combiner plusieurs filtres :
GET /prestations/?categorie=1&prix_min=50&prix_max=200&tags=1,2&ordering=-note_moyenne&search=windows

📌 Mes avis :
GET /avis/?mes_avis=true

📌 Avis d'une prestation :
GET /avis/?prestation=5


╔══════════════════════════════════════════════════════════════════════════╗
║                      EXEMPLES DE REQUÊTES POST                            ║
╚══════════════════════════════════════════════════════════════════════════╝

📌 Créer une prestation :
POST /prestations/
{
  "titre": "Réparation ordinateur",
  "description": "Diagnostic et réparation rapide",
  "prix": 50.00,
  "categorie": 1,
  "tags": [1, 2],
  "est_actif": true
}

📌 Créer un avis :
POST /avis/
{
  "prestation": 5,
  "note": 5,
  "commentaire": "Excellent service, très rapide !"
}

📌 Modifier une prestation :
PATCH /prestations/5/
{
  "prix": 45.00
}

"""