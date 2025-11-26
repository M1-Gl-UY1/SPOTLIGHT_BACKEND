from rest_framework import serializers
from .models import Categorie, Tag, Prestation, Image, Avis


class CategorieSerializer(serializers.ModelSerializer):
    """Serializer pour les catégories"""
    sous_categories = serializers.StringRelatedField(many=True, read_only=True)
    nb_prestations = serializers.SerializerMethodField()
    
    class Meta:
        model = Categorie
        fields = ['id', 'nom', 'description', 'parent', 'sous_categories', 'nb_prestations']
    
    def get_nb_prestations(self, obj):
        return obj.prestations.filter(est_actif=True).count()


class TagSerializer(serializers.ModelSerializer):
    """Serializer pour les tags"""
    class Meta:
        model = Tag
        fields = ['id', 'nom']


class ImageSerializer(serializers.ModelSerializer):
    """Serializer pour les images"""
    class Meta:
        model = Image
        fields = ['id', 'url', 'ordre']


class AvisSerializer(serializers.ModelSerializer):
    """Serializer pour les avis"""
    utilisateur_nom = serializers.CharField(source='utilisateur.username', read_only=True)
    
    class Meta:
        model = Avis
        fields = ['id', 'note', 'commentaire', 'date', 'utilisateur_nom']
        read_only_fields = ['date', 'utilisateur_nom']


class AvisCreateSerializer(serializers.ModelSerializer):
    """Serializer pour créer un avis"""
    class Meta:
        model = Avis
        fields = ['prestation', 'note', 'commentaire']
    
    def validate(self, data):
        user = self.context['request'].user
        prestation = data['prestation']
        
        # Vérifie si l'utilisateur a déjà laissé un avis
        if Avis.objects.filter(utilisateur=user, prestation=prestation).exists():
            raise serializers.ValidationError(
                "Vous avez déjà laissé un avis pour cette prestation."
            )
        return data
    
    def create(self, validated_data):
        validated_data['utilisateur'] = self.context['request'].user
        return super().create(validated_data)


class PrestationListSerializer(serializers.ModelSerializer):
    """Serializer léger pour la liste des prestations"""
    categorie_nom = serializers.CharField(source='categorie.nom', read_only=True)
    tags = TagSerializer(many=True, read_only=True)
    image_principale = serializers.SerializerMethodField()
    
    class Meta:
        model = Prestation
        fields = [
            'id', 'titre', 'prix', 'categorie_nom', 'tags',
            'note_moyenne', 'nb_avis', 'popularite', 'image_principale'
        ]
    
    def get_image_principale(self, obj):
        image = obj.images.first()
        return image.url if image else None


class PrestationDetailSerializer(serializers.ModelSerializer):
    """Serializer complet pour le détail d'une prestation"""
    categorie = CategorieSerializer(read_only=True)
    tags = TagSerializer(many=True, read_only=True)
    images = ImageSerializer(many=True, read_only=True)
    avis = AvisSerializer(many=True, read_only=True)
    
    class Meta:
        model = Prestation
        fields = [
            'id', 'titre', 'description', 'prix', 'categorie', 'tags',
            'note_moyenne', 'nb_avis', 'popularite', 'date_creation',
            'images', 'avis'
        ]


class PrestationCreateSerializer(serializers.ModelSerializer):
    """Serializer pour créer/modifier une prestation"""
    class Meta:
        model = Prestation
        fields = [
            'titre', 'description', 'prix', 'categorie', 
            'tags', 'est_actif'
        ]