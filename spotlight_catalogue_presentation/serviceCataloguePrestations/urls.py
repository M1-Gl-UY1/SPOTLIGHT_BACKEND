from django.urls import path, include
from rest_framework.routers import DefaultRouter

from .views import (
    ServiceViewSet,      # Renommé de PrestationViewSet
    CategorieViewSet,
    TagViewSet,
    ai_search_view,
    ai_enhance_description_view,
    ai_suggest_response_view
)

# Création du router
router = DefaultRouter()

# Enregistrement des ViewSets
# On mappe 'services' au lieu de 'prestations' pour respecter le contrat
router.register(r'services', ServiceViewSet, basename='service')

# Ces routes sont utiles pour le frontend (dropdowns filtres) même si pas dans le résumé principal
router.register(r'categories', CategorieViewSet, basename='categorie')
router.register(r'tags', TagViewSet, basename='tag')


# URLs de l'application
urlpatterns = [
    # Routes CRUD standard (/services/, /categories/, etc.)
    path('', include(router.urls)),

    # --- ROUTES IA (AI-SERVICE) ---
    # GET /ai/search
    path('ai/search/', ai_search_view, name='ai-search'),
    
    # POST /ai/enhance-description
    path('ai/enhance-description/', ai_enhance_description_view, name='ai-enhance-description'),
    
    # POST /ai/suggest-response
    path('ai/suggest-response/', ai_suggest_response_view, name='ai-suggest-response'),
]