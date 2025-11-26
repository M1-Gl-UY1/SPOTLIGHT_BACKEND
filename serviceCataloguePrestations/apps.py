from django.apps import AppConfig
from django.conf import settings


class ServicecatalogueprestationsConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'serviceCataloguePrestations'
    
    def ready(self):
        """
        Cette méthode s'exécute au démarrage de Django
        """
        # Évite de lancer 2 fois en mode dev (à cause du reloader)
        import os
        if os.environ.get('RUN_MAIN') == 'true' or settings.DEBUG == False:
            
            # Vérifie si Eureka est activé
            if getattr(settings, 'ENABLE_EUREKA', False):
                try:
                    # Import ici pour éviter les problèmes de circular import
                    from spotlight.eureka_config import eureka_manager
                    
                    # Démarre l'intégration Spring Cloud
                    eureka_manager.start()
                    
                except Exception as e:
                    print(f"⚠️ Erreur lors du démarrage Eureka : {e}")
                    print("Le service fonctionne en mode standalone")


# ============================================================================
# À ajouter dans ton __init__.py de l'application
# ============================================================================

# __init__.py (dans le même dossier que apps.py)
"""
default_app_config = 'catalogue.apps.CatalogueConfig'
"""