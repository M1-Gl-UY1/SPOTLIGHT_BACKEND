import json
import os
from typing import List, Dict, Any
from django.db.models import Q
from .models import Prestation, Categorie, Tag
from openai import OpenAI


class PrestationAIService:
    """Service d'IA pour analyser les besoins et recommander des prestations"""

    def __init__(self):
        api_key = os.environ.get("OPENROUTER_API_KEY")
        if not api_key:
            raise ValueError("❌ La variable OPENROUTER_API_KEY est manquante.")

        self.client = OpenAI(
            base_url="https://openrouter.ai/api/v1",
            api_key=api_key,
        )

        self.model = "openai/gpt-4o-mini"

    # ---------------------------------------------------------------------
    def analyser_besoin(self, description_besoin: str) -> Dict[str, Any]:
        categories = list(Categorie.objects.values_list('nom', flat=True))
        tags = list(Tag.objects.values_list('nom', flat=True))

        prompt = f"""
Tu es un assistant intelligent pour un service de prestations.

Analyse ce besoin client reformule le correctement avec un message copywriting en utilisant la methode AIDA et retourne un JSON propre.

BESOIN CLIENT : "{description_besoin}"

CATÉGORIES DISPONIBLES : {', '.join(categories)}
TAGS DISPONIBLES : {', '.join(tags)}

Retourne STRICTEMENT ce JSON :
{{
    "categorie": "...",
    "tags": [...],
    "urgence": "basse|moyenne|haute",
    "budget_min": nombre ou null,
    "budget_max": nombre ou null,
    "mots_cles": [...],
    "resume": "...",
}}
"""

        try:
            response = self.client.chat.completions.create(
                model=self.model,
                messages=[{"role": "user", "content": prompt}],
            )

            # -------- FIX ICI ----------
            text = response.choices[0].message.content.strip()
            # ----------------------------

            if text.startswith("```"):
                text = text.split("```")[1]
                if text.startswith("json"):
                    text = text[4:]

            return json.loads(text)

        except Exception as e:
            print("Erreur IA :", e)
            return {
                "categorie": None,
                "tags": [],
                "urgence": "moyenne",
                "budget_min": None,
                "budget_max": None,
                "mots_cles": description_besoin.split(),
                "resume": description_besoin,
                "perno": "idriss toune"
            }

    # ---------------------------------------------------------------------
    def rechercher_prestations(self, analyse: Dict[str, Any]) -> List[Prestation]:
        queryset = Prestation.objects.filter(est_actif=True)

        if analyse.get("categorie"):
            try:
                categorie = Categorie.objects.get(nom__iexact=analyse["categorie"])
                queryset = queryset.filter(categorie=categorie)
            except Categorie.DoesNotExist:
                pass

        if analyse.get("budget_max"):
            queryset = queryset.filter(prix__lte=analyse["budget_max"])
        if analyse.get("budget_min"):
            queryset = queryset.filter(prix__gte=analyse["budget_min"])

        if analyse.get("tags"):
            tag_objects = Tag.objects.filter(nom__in=analyse["tags"])
            if tag_objects.exists():
                queryset = queryset.filter(tags__in=tag_objects).distinct()

        if analyse.get("mots_cles"):
            q_objects = Q()
            for mot in analyse["mots_cles"]:
                q_objects |= Q(titre__icontains=mot) | Q(description__icontains=mot)
            queryset = queryset.filter(q_objects)

        if analyse.get("urgence") == "haute":
            queryset = queryset.order_by("-note_moyenne", "-popularite")
        else:
            queryset = queryset.order_by("-popularite", "-note_moyenne")

        return list(queryset[:5])

    # ---------------------------------------------------------------------
    def generer_offre(self, description_besoin, prestations, analyse):

        prestations_info = [{
            "id": p.id,
            "titre": p.titre,
            "description": p.description[:200],
            "prix": float(p.prix),
            "note": p.note_moyenne,
            "nb_avis": p.nb_avis
        } for p in prestations]

        prompt = f"""
Tu es un conseiller commercial expert.

BESOIN : "{description_besoin}"

PRESTATIONS :
{json.dumps(prestations_info, indent=2, ensure_ascii=False)}

Génère un JSON strict :
{{
    "titre_offre": "...",
    "introduction": "...",
    "recommandations": [...],
    "estimation_totale": ...,
    "duree_estimee": "...",
    "conseils": [...],
    "prochaines_etapes": [...]
}}
"""

        try:
            response = self.client.chat.completions.create(
                model=self.model,
                messages=[{"role": "user", "content": prompt}],
            )

            # -------- FIX ICI ----------
            text = response.choices[0].message.content.strip()
            # ----------------------------

            if text.startswith("```"):
                text = text.split("```")[1]
                if text.startswith("json"):
                    text = text[4:]

            offre = json.loads(text)

            offre["prestations"] = []
            for reco in offre.get("recommandations", []):
                p = next((x for x in prestations if x.id == reco.get("prestation_id")), None)
                if p:
                    offre["prestations"].append({
                        "id": p.id,
                        "titre": p.titre,
                        "description": p.description,
                        "prix": float(p.prix),
                        "note_moyenne": p.note_moyenne,
                        "raison_recommandation": reco.get("raison", ""),
                        "priorite": reco.get("priorite", 1)
                    })

            return offre

        except Exception as e:
            print("Erreur IA :", e)
            return {
                "titre_offre": "Offre de prestations",
                "prestations": [{
                    "id": p.id,
                    "titre": p.titre,
                    "description": p.description,
                    "prix": float(p.prix),
                    "note_moyenne": p.note_moyenne,
                    "priorite": i + 1
                } for i, p in enumerate(prestations)],
                "estimation_totale": sum(float(p.prix) for p in prestations),
                "conseils": ["Contactez-nous pour plus d'informations"]
            }

    # ---------------------------------------------------------------------
    def recommander_offre_complete(self, description_besoin: str) -> Dict[str, Any]:

        analyse = self.analyser_besoin(description_besoin)
        prestations = self.rechercher_prestations(analyse)

        if not prestations:
            return {
                "erreur": "Aucune prestation trouvée",
                "analyse": analyse,
                "message": "Essayez de reformuler votre demande."
            }

        offre = self.generer_offre(description_besoin, prestations, analyse)

        offre["analyse"] = analyse
        offre["nombre_prestations"] = len(prestations)

        return offre


ai_service = PrestationAIService()
