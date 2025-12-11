# eureka_config.py
# Fichier à créer dans ton projet Django (à côté de settings.py)

import py_eureka_client.eureka_client as eureka_client
import requests
from django.conf import settings

class EurekaConfigManager:
    """
    Gère l'enregistrement Eureka et la récupération de config
    depuis Spring Cloud Config Server
    """

    def __init__(self):
        # Configuration de base (à adapter selon ton environnement)
        self.app_name = "catalogue-prestations"  # Nom dans Eureka
        self.instance_port = 8000  # Port Django
        self.instance_host = "registry-service"  # ou ton IP

        # URLs de tes services Spring
        self.eureka_server = "http://registry-service:8761/"
        self.config_server = "http://config-service:8888"

        # Configuration récupérée du Config Server
        self.remote_config = {}

    def fetch_config_from_spring(self):
        """
        Récupère la configuration depuis Spring Cloud Config Server
        """
        try:
            # URL du Config Server
            # Format : http://config-server:8888/{app-name}/{profile}
            config_url = f"{self.config_server}/{self.app_name}/default"

            print(f"📡 Récupération de la config depuis : {config_url}")

            response = requests.get(config_url, timeout=5)

            if response.status_code == 200:
                data = response.json()

                # Spring Config retourne les propriétés dans 'propertySources'
                if 'propertySources' in data:
                    for source in data['propertySources']:
                        if 'source' in source:
                            self.remote_config.update(source['source'])

                print(f" Configuration récupérée : {len(self.remote_config)} propriétés")
                return True
            else:
                print(f" Config Server inaccessible (code {response.status_code})")
                return False

        except Exception as e:
            print(f" Erreur lors de la récupération de la config : {e}")
            return False

    def get_config_value(self, key, default=None):
        """
        Récupère une valeur de configuration
        """
        return self.remote_config.get(key, default)

    def register_to_eureka(self):
        """
        Enregistre le service Django dans Eureka
        """
        try:
            print(f"📡 Enregistrement dans Eureka : {self.eureka_server}")

            # Initialisation du client Eureka
            eureka_client.init(
                eureka_server=self.eureka_server,
                app_name=self.app_name,
                instance_port=self.instance_port,
                instance_host=self.instance_host,

                # Configuration du heartbeat
                renewal_interval_in_secs=30,  # Envoie heartbeat toutes les 30s
                duration_in_secs=90,  # Expire après 90s sans heartbeat

                # URL de health check
                health_check_url=f"http://{self.instance_host}:{self.instance_port}/health/",
                status_page_url=f"http://{self.instance_host}:{self.instance_port}/info/",
                home_page_url=f"http://{self.instance_host}:{self.instance_port}/",

                # Métadonnées supplémentaires
                metadata={
                    "framework": "Django",
                    "version": "1.0.0",
                    "description": "Service de catalogue des prestations"
                }
            )

            print(f"Service enregistré dans Eureka sous le nom : {self.app_name}")
            return True

        except Exception as e:
            print(f" Erreur lors de l'enregistrement Eureka : {e}")
            return False

    def start(self):
        """
        Démarre l'intégration : Config + Eureka
        """
        print(" Démarrage de l'intégration Spring Cloud...")

        # 1. Récupère d'abord la configuration
        config_success = self.fetch_config_from_spring()

        # 2. Puis enregistre dans Eureka
        eureka_success = self.register_to_eureka()

        if config_success and eureka_success:
            print(" Intégration Spring Cloud réussie !")
        else:
            print("️ Intégration partielle - vérifiez vos services Spring")


# Instance globale
eureka_manager = EurekaConfigManager()