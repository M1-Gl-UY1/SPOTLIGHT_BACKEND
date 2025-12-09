from rest_framework import status, serializers
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from .ai_service import ai_service
from .models import Prestation
from .serializers import PrestationListSerializer


# ============================================================================
# SERIALIZERS
# ============================================================================

class BesoinRequestSerializer(serializers.Serializer):
    """Serializer pour la requête de besoin"""
    description = serializers.CharField(max_length=1000)
    budget_max = serializers.DecimalField(
        max_digits=10,
        decimal_places=2,
        required=False,
        allow_null=True
    )
    urgence = serializers.ChoiceField(
        choices=['basse', 'moyenne', 'haute'],
        required=False,
        default='moyenne'
    )


class OffreAISerializer(serializers.Serializer):
    """Serializer pour l'offre générée par l'IA"""
    titre_offre = serializers.CharField()
    introduction = serializers.CharField()
    prestations = serializers.ListField()
    estimation_totale = serializers.FloatField()
    duree_estimee = serializers.CharField(required=False)
    conseils = serializers.ListField(required=False)
    prochaines_etapes = serializers.ListField(required=False)
    analyse = serializers.DictField()
    nombre_prestations = serializers.IntegerField()


# ============================================================================
# VIEWS
# ============================================================================

class RecommandationAIView(APIView):
    """ génère une recommandation IA complète """
    permission_classes = [AllowAny]

    def post(self, request):

        # 1. Validation
        serializer = BesoinRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=400)

        description = serializer.validated_data['description']
        budget_max = serializer.validated_data.get('budget_max')
        urgence = serializer.validated_data.get('urgence', 'moyenne')

        # 2. Construire description enrichie
        description_enrichie = description

        if budget_max is not None:
            description_enrichie += f" Budget maximum : {float(budget_max)}€."

        description_enrichie += f" Urgence : {urgence}."

        try:
            # 3. Pipeline IA ➝ offre complète
            offre = ai_service.recommander_offre_complete(description_enrichie)

            if 'erreur' in offre:
                return Response(offre, status=404)

            output = OffreAISerializer(data=offre)
            if output.is_valid():
                return Response(output.validated_data)

            return Response(offre)

        except Exception as e:
            return Response(
                {"erreur": "Erreur lors de la génération de la recommandation", "detail": str(e)},
                status=500
            )


@api_view(['POST'])
@permission_classes([AllowAny])
def analyse_besoin_simple(request):
    """ Analyse IA seule + prestations correspondantes """

    description = request.data.get('description')
    if not description:
        return Response({"erreur": 'Le champ "description" est requis'}, status=400)

    try:
        analyse = ai_service.analyser_besoin(description)
        prestations = ai_service.rechercher_prestations(analyse)

        prestations_data = PrestationListSerializer(prestations, many=True).data

        return Response({
            "analyse": analyse,
            "prestations_trouvees": len(prestations),
            "prestations": prestations_data
        })

    except Exception as e:
        return Response({"erreur": str(e)}, status=500)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def sauvegarder_offre(request):
    """ Placeholder pour sauvegarde d'offre """
    return Response({
        "message": "Offre sauvegardée",
        "acceptee": request.data.get("acceptee", False)
    })


@api_view(['GET'])
@permission_classes([AllowAny])
def exemples_besoins(request):

    
    
    """ Exemples préfaits pour l’utilisateur """

    exemples = [
        {
            "titre": "Réparation urgente",
            "description": "Mon ordinateur portable ne démarre plus depuis ce matin. J'en ai besoin rapidement.",
            "categorie": "Informatique"
        },
        {
            "titre": "Installation logiciel",
            "description": "Je veux installer Windows 11 et configurer mes logiciels.",
            "categorie": "Informatique"
        },
        {
            "titre": "Dépannage à domicile",
            "description": "Ma machine à laver ne vidange plus.",
            "categorie": "Électroménager"
        }
    ]

    return Response(exemples)
