# health_views.py
# Créer ce fichier dans ton application Django

from django.http import JsonResponse
from django.db import connection
from django.conf import settings
import sys

def health_check(request):
    """
    Endpoint de santé pour Eureka
    URL : /health/
    """
    try:
        # Test de connexion à la base de données
        with connection.cursor() as cursor:
            cursor.execute("SELECT 1")
        
        db_status = "UP"
    except Exception as e:
        db_status = "DOWN"
    
    health_data = {
        "status": "UP" if db_status == "UP" else "DOWN",
        "service": "catalogue-prestations",
        "version": "1.0.0",
        "checks": {
            "database": db_status,
            "django": "UP"
        }
    }
    
    status_code = 200 if health_data["status"] == "UP" else 503
    return JsonResponse(health_data, status=status_code)


def info_endpoint(request):
    """
    Endpoint d'informations pour Eureka
    URL : /info/
    """
    info_data = {
        "app": {
            "name": "Catalogue Prestations",
            "description": "Service de gestion du catalogue des prestations",
            "version": "1.0.0",
            "framework": "Django",
            "python_version": f"{sys.version_info.major}.{sys.version_info.minor}.{sys.version_info.micro}"
        },
        "build": {
            "artifact": "catalogue-prestations",
            "group": "com.monentreprise.microservices"
        }
    }
    
    return JsonResponse(info_data)


# urls.py - Ajouter ces routes dans ton fichier urls.py principal
"""
from django.urls import path
from .health_views import health_check, info_endpoint

urlpatterns = [
    # ... tes autres URLs
    path('health/', health_check, name='health'),
    path('info/', info_endpoint, name='info'),
]
"""