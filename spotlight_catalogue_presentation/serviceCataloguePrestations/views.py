# views.py

from typing import Generic
from rest_framework import viewsets, filters, status
from rest_framework.decorators import action, api_view
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticatedOrReadOnly, IsAuthenticated, AllowAny
from rest_framework.parsers import MultiPartParser, FormParser
from django_filters.rest_framework import DjangoFilterBackend
from django.db.models import Q, Count
from django.contrib.auth.models import User

# Imports modèles
from .models import Categorie, Tag, Prestation, Avis, Image

# Imports serializers
from .serializers import (
    CategorieSerializer, TagSerializer,
    PrestationListSerializer, PrestationDetailSerializer, 
    PrestationCreateSerializer, AvisSerializer, AvisCreateSerializer, userSerializer
)

# --- 1. VIEWSETS (CRUD STANDARD) ---

class CategorieViewSet(viewsets.ReadOnlyModelViewSet):
    """
    GET /categories/ - Liste toutes les catégories
    GET /categories/{id}/ - Détail d'une catégorie
    """
    queryset = Categorie.objects.all()
    serializer_class = CategorieSerializer
    
    @action(detail=True, methods=['get'])
    def prestations(self, request, pk=None):
        """Récupère toutes les prestations d'une catégorie"""
        categorie = self.get_object()
        prestations = categorie.prestations.filter(est_actif=True)
        serializer = PrestationListSerializer(prestations, many=True)
        return Response(serializer.data)


class TagViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = Tag.objects.all()
    serializer_class = TagSerializer


class ServiceViewSet(viewsets.ModelViewSet): # Renommé de PrestationViewSet
    """
    Contrat: 
    GET /services/ - Liste des services
    GET /services/{id}/ - Détail d'un service
    POST /services/ - Créer
    PUT /services/{id}/ - Modifier
    POST /services/{id}/media - Upload de fichier
    """
    queryset = Prestation.objects.filter(est_actif=True).select_related('categorie').prefetch_related('tags', 'images')
    permission_classes = [AllowAny] # Mettre IsAuthenticatedOrReadOnly en prod
    
    # Filtres et Recherche
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    search_fields = ['titre', 'description']
    ordering_fields = ['prix', 'popularite', 'note_moyenne', 'date_creation']
    ordering = ['-popularite']
    
    # Pour l'upload de fichiers
    parser_classes = (MultiPartParser, FormParser)

    def get_serializer_class(self):
        if self.action == 'list':
            return PrestationListSerializer
        elif self.action in ['create', 'update', 'partial_update']:
            return PrestationCreateSerializer
        return PrestationDetailSerializer
    
    def get_queryset(self):
        queryset = super().get_queryset()
        
        # Filtres (identique à votre code précédent)
        categorie = self.request.query_params.get('categorie')
        if categorie:
            queryset = queryset.filter(categorie_id=categorie)
        
        prix_min = self.request.query_params.get('prix_min')
        prix_max = self.request.query_params.get('prix_max')
        if prix_min:
            queryset = queryset.filter(prix__gte=prix_min)
        if prix_max:
            queryset = queryset.filter(prix__lte=prix_max)
        
        tags = self.request.query_params.get('tags')
        if tags:
            tag_ids = [int(t) for t in tags.split(',')]
            queryset = queryset.filter(tags__id__in=tag_ids).distinct()
            
        return queryset

    # --- ACTION D'UPLOAD DE MEDIA (MANQUANTE) ---
    @action(detail=True, methods=['post'], url_path='media')
    def upload_media(self, request, pk=None):
        service = self.get_object()
        file_obj = request.FILES.get('file')
        
        if file_obj:
            # Création de l'objet Image lié à la Prestation
            image = Image.objects.create(prestation=service, fichier=file_obj) # Vérifiez le nom du champ 'fichier' dans votre modèle Image
            return Response({'status': 'Media uploaded', 'url': image.fichier.url}, status=status.HTTP_201_CREATED)
        
        return Response({'error': 'No file provided'}, status=status.HTTP_400_BAD_REQUEST)

    # --- ACTIONS SPÉCIALES (CONSERVÉES) ---
    @action(detail=False, methods=['get'])
    def populaires(self, request):
        prestations = self.get_queryset().order_by('-popularite')[:10]
        serializer = PrestationListSerializer(prestations, many=True)
        return Response(serializer.data)


class AvisViewSet(viewsets.ModelViewSet):
    # Identique à votre code
    queryset = Avis.objects.all().select_related('prestation', 'utilisateur')
    permission_classes = [AllowAny]
    
    def get_serializer_class(self):
        if self.action == 'create':
            return AvisCreateSerializer
        return AvisSerializer
    
    def perform_create(self, serializer):
        # En prod : serializer.save(utilisateur=self.request.user)
        serializer.save()


class UserViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = User.objects.all()
    serializer_class = userSerializer
    permission_classes = [AllowAny]


# --- 2. VUES IA (CORRIGÉES POUR LE CONTRAT) ---

# GET /ai/search
@api_view(['GET'])
def ai_search_view(request):
    """Recherche intelligente via IA"""
    query = request.GET.get('q', '')
    # ... Logique IA ...
    return Response({'results': []})

# POST /ai/enhance-description
@api_view(['POST'])
def ai_enhance_description_view(request):
    """Améliorer la description d'un service"""
    description = request.data.get('description', '')
    # ... Logique IA ...
    return Response({'enhanced': description + " [Amélioré]"})

# POST /ai/suggest-response
@api_view(['POST'])
def ai_suggest_response_view(request):
    """Suggérer une réponse pour le chat"""
    message = request.data.get('message', '')
    # ... Logique IA ...
    return Response({'suggestion': "Voici une réponse suggérée."})