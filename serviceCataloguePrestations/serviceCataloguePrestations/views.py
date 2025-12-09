from rest_framework import viewsets, filters, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticatedOrReadOnly, IsAuthenticated
from django_filters.rest_framework import DjangoFilterBackend
from django.db.models import Q, Count
from .models import Categorie, Tag, Prestation, Avis
from .serializers import (
    CategorieSerializer, TagSerializer,
    PrestationListSerializer, PrestationDetailSerializer, 
    PrestationCreateSerializer, AvisSerializer, AvisCreateSerializer
)

from spotlight.eureka_config import eureka_manager

class CategorieViewSet(viewsets.ReadOnlyModelViewSet):
    """
    ViewSet pour consulter les catégories
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
    """
    ViewSet pour consulter les tags
    GET /tags/ - Liste tous les tags
    GET /tags/{id}/ - Détail d'un tag
    """
    queryset = Tag.objects.all()
    serializer_class = TagSerializer


class PrestationViewSet(viewsets.ModelViewSet):
    """
    ViewSet complet pour les prestations avec recherche et filtres
    
    GET /prestations/ - Liste des prestations (avec filtres)
    GET /prestations/{id}/ - Détail d'une prestation
    POST /prestations/ - Créer une prestation (admin)
    PUT/PATCH /prestations/{id}/ - Modifier (admin)
    DELETE /prestations/{id}/ - Supprimer (admin)
    
    Filtres disponibles:
    - ?categorie=1
    - ?prix_min=10&prix_max=100
    - ?tags=1,2,3
    - ?search=mot-clé
    - ?ordering=prix / -prix / popularite / -popularite / note_moyenne
    """
    queryset = Prestation.objects.filter(est_actif=True).select_related('categorie').prefetch_related('tags', 'images')
    permission_classes = [IsAuthenticatedOrReadOnly]
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    search_fields = ['titre', 'description']
    ordering_fields = ['prix', 'popularite', 'note_moyenne', 'date_creation']
    ordering = ['-popularite']
    
    def get_serializer_class(self):
        if self.action == 'list':
            return PrestationListSerializer
        elif self.action in ['create', 'update', 'partial_update']:
            return PrestationCreateSerializer
        return PrestationDetailSerializer
    
    def get_queryset(self):
        queryset = super().get_queryset()
        
        # Filtredestroy par catégorie
        categorie = self.request.query_params.get('categorie')
        if categorie:
            queryset = queryset.filter(categorie_id=categorie)
        
        # Filtre par prix
        prix_min = self.request.query_params.get('prix_min')
        prix_max = self.request.query_params.get('prix_max')
        if prix_min:
            queryset = queryset.filter(prix__gte=prix_min)
        if prix_max:
            queryset = queryset.filter(prix__lte=prix_max)
        
        # Filtre par tags (plusieurs tags possibles)
        tags = self.request.query_params.get('tags')
        if tags:
            tag_ids = [int(t) for t in tags.split(',')]
            queryset = queryset.filter(tags__id__in=tag_ids).distinct()
        
        # Filtre par note minimale
        note_min = self.request.query_params.get('note_min')
        if note_min:
            queryset = queryset.filter(note_moyenne__gte=note_min)
        
        return queryset
    
    @action(detail=False, methods=['get'])
    def populaires(self, request):
        """Retourne les prestations les plus populaires"""
        prestations = self.get_queryset().order_by('-popularite')[:10]
        serializer = PrestationListSerializer(prestations, many=True)
        return Response(serializer.data)
    
    @action(detail=False, methods=['get'])
    def meilleures_notes(self, request):
        """Retourne les prestations les mieux notées"""
        prestations = self.get_queryset().filter(nb_avis__gte=5).order_by('-note_moyenne')[:10]
        serializer = PrestationListSerializer(prestations, many=True)
        return Response(serializer.data)
    
    @action(detail=False, methods=['get'])
    def nouveautes(self, request):
        """Retourne les prestations les plus récentes"""
        prestations = self.get_queryset().order_by('-date_creation')[:10]
        serializer = PrestationListSerializer(prestations, many=True)
        return Response(serializer.data)
    
    @action(detail=True, methods=['get'])
    def recommandations(self, request, pk=None):
        """Recommandations basées sur une prestation"""
        prestation = self.get_object()
        
        # Prestations similaires : même catégorie ou tags communs
        similaires = Prestation.objects.filter(
            Q(categorie=prestation.categorie) | Q(tags__in=prestation.tags.all()),
            est_actif=True
        ).exclude(id=prestation.id).distinct().order_by('-note_moyenne')[:5]
        
        serializer = PrestationListSerializer(similaires, many=True)
        return Response(serializer.data)
    
    @action(detail=True, methods=['post'], permission_classes=[IsAuthenticated])
    def incrementer_popularite(self, request, pk=None):
        """Incrémente la popularité (vue, like, etc.)"""
        prestation = self.get_object()
        prestation.popularite += 1
        prestation.save()
        return Response({'popularite': prestation.popularite})


class AvisViewSet(viewsets.ModelViewSet):
    """
    ViewSet pour gérer les avis
    
    GET /avis/ - Liste des avis
    POST /avis/ - Créer un avis (authentifié)
    PUT/PATCH /avis/{id}/ - Modifier son avis
    DELETE /avis/{id}/ - Supprimer son avis
    """
    queryset = Avis.objects.all().select_related('prestation', 'utilisateur')
    permission_classes = [IsAuthenticatedOrReadOnly]
    
    def get_serializer_class(self):
        if self.action == 'create':
            return AvisCreateSerializer
        return AvisSerializer
    
    def get_queryset(self):
        queryset = super().get_queryset()
        
        # Filtre par prestation
        prestation = self.request.query_params.get('prestation')
        if prestation:
            queryset = queryset.filter(prestation_id=prestation)
        
        # Filtre par utilisateur (mes avis)
        if self.request.query_params.get('mes_avis') == 'true':
            if self.request.user.is_authenticated:
                queryset = queryset.filter(utilisateur=self.request.user)
        
        return queryset
    
    def perform_create(self, serializer):
        serializer.save(utilisateur=self.request.user)
    
    def perform_update(self, serializer):
        # Vérifie que l'utilisateur modifie bien son propre avis
        if serializer.instance.utilisateur != self.request.user:
            raise PermissionError("Vous ne pouvez modifier que vos propres avis")
        serializer.save()
    
    def perform_destroy(self, instance):
        # Vérifie que l'utilisateur supprime bien son propre avis
        if instance.utilisateur != self.request.user:
            raise PermissionError("Vous ne pouvez supprimer que vos propres avis")
        instance.delete()
        
        
        
        
        
        
        
        
        
# ============================================================================
# Configuration pour l'enregistrement du service auprès d'Eureka
# ============================================================================
