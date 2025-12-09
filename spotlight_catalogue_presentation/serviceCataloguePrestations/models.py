from django.db import models
from django.contrib.auth.models import User
from django.core.validators import MinValueValidator, MaxValueValidator
from django.core.serializers.json import DjangoJSONEncoder

class Categorie(models.Model):
    """Catégorie de prestations (peut être imbriquée)"""
    nom = models.CharField(max_length=100, unique=True)
    description = models.TextField(blank=True, null=True)
    parent = models.ForeignKey(
        'self', 
        on_delete=models.CASCADE, 
        null=True, 
        blank=True,
        related_name='sous_categories'
    )
    
    class Meta:
        verbose_name = "Catégorie"
        verbose_name_plural = "Catégories"
        ordering = ['nom']
    
    def __str__(self):
        return self.nom


class Tag(models.Model):
    """Tags pour filtrage avancé"""
    nom = models.CharField(max_length=50, unique=True)
    
    class Meta:
        verbose_name = "Tag"
        verbose_name_plural = "Tags"
        ordering = ['nom']
    
    def __str__(self):
        return self.nom


class Prestation(models.Model):
    """Prestation/Service proposé"""
    titre = models.CharField(max_length=200)
    description = models.TextField()
    prix = models.DecimalField(max_digits=10, decimal_places=2)
    categorie = models.ForeignKey(
        Categorie, 
        on_delete=models.PROTECT,  # Empêche suppression si prestations liées
        related_name='prestations'
    )
    tags = models.ManyToManyField(Tag, blank=True, related_name='prestations')
    
    # Champs pour recommandations et tri
    note_moyenne = models.FloatField(default=0.0)
    nb_avis = models.IntegerField(default=0)
    popularite = models.IntegerField(default=0)  # vues, likes, etc.
    
    # Métadonnées
    date_creation = models.DateTimeField(auto_now_add=True)
    est_actif = models.BooleanField(default=True)
    
    class Meta:
        verbose_name = "Prestation"
        verbose_name_plural = "Prestations"
        ordering = ['-popularite', '-note_moyenne']
        indexes = [
            models.Index(fields=['categorie', 'prix']),
            models.Index(fields=['popularite']),
            models.Index(fields=['note_moyenne']),
        ]
    
    def __str__(self):
        return self.titre
    
    def mettre_a_jour_note(self):
        """Recalcule la note moyenne et le nombre d'avis"""
        avis = self.avis.all()
        self.nb_avis = avis.count()
        if self.nb_avis > 0:
            self.note_moyenne = avis.aggregate(models.Avg('note'))['note__avg']
        else:
            self.note_moyenne = 0.0
        self.save()


class Image(models.Model):
    """Images d'une prestation"""
    prestation = models.ForeignKey(
        Prestation, 
        on_delete=models.CASCADE,  # Suppression en cascade
        related_name='images'
    )
    url = models.URLField(max_length=500)
    ordre = models.IntegerField(default=0)
    
    class Meta:
        verbose_name = "Image"
        verbose_name_plural = "Images"
        ordering = ['ordre']
    
    def __str__(self):
        return f"Image {self.ordre} - {self.prestation.titre}"


class Avis(models.Model):
    """Avis/Évaluation d'une prestation"""
    prestation = models.ForeignKey(
        Prestation,
        on_delete=models.CASCADE,  # Suppression en cascade
        related_name='avis'
    )
    utilisateur = models.ForeignKey(
        User,
        on_delete=models.SET_NULL,  # Anonymisation si user supprimé
        null=True,
        related_name='avis'
    )
    note = models.IntegerField(
        validators=[MinValueValidator(1), MaxValueValidator(5)]
    )
    commentaire = models.TextField(blank=True)
    date = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        verbose_name = "Avis"
        verbose_name_plural = "Avis"
        ordering = ['-date']
        unique_together = ['prestation', 'utilisateur']  # 1 avis par user/prestation
    
    def __str__(self):
        user_name = self.utilisateur.username if self.utilisateur else "Anonyme"
        return f"Avis de {user_name} sur {self.prestation.titre}"
    
    def save(self, *args, **kwargs):
        super().save(*args, **kwargs)
        # Met à jour automatiquement la note moyenne de la prestation
        self.prestation.mettre_a_jour_note()
        
        


# ======================================================================================================
#                                                                                                      =
#                                                                                                      =
# ======================================================================================================




class OffrePersonnalisee(models.Model):
    """
    Sauvegarde les offres personnalisées générées par l'IA
    """
    
    utilisateur = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        related_name='offres_ia',
        null=True,
        blank=True,
        help_text="Utilisateur ayant reçu l'offre (null si anonyme)"
    )
    
    # Besoin original
    description_besoin = models.TextField(
        help_text="Description du besoin exprimé par l'utilisateur"
    )
    
    # Analyse IA
    analyse_json = models.JSONField(
        encoder=DjangoJSONEncoder,
        help_text="Analyse structurée du besoin par l'IA"
    )
    
    # Offre générée
    offre_json = models.JSONField(
        encoder=DjangoJSONEncoder,
        help_text="Offre complète générée par l'IA"
    )
    
    # Prestations recommandées (relation many-to-many)
    prestations = models.ManyToManyField(
        'Prestation',
        related_name='offres_ia',
        through='OffrePrestation'
    )
    
    # Statut
    STATUS_CHOICES = [
        ('generee', 'Générée'),
        ('consultee', 'Consultée'),
        ('acceptee', 'Acceptée'),
        ('refusee', 'Refusée'),
        ('en_cours', 'En cours'),
        ('terminee', 'Terminée'),
    ]
    
    statut = models.CharField(
        max_length=20,
        choices=STATUS_CHOICES,
        default='generee'
    )
    
    # Feedback utilisateur
    note_offre = models.IntegerField(
        null=True,
        blank=True,
        help_text="Note de 1 à 5 donnée par l'utilisateur"
    )
    
    commentaire_utilisateur = models.TextField(
        blank=True,
        help_text="Commentaire de l'utilisateur sur l'offre"
    )
    
    # Prix
    estimation_totale = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        help_text="Prix total estimé"
    )
    
    prix_final = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        null=True,
        blank=True,
        help_text="Prix réel si l'offre est acceptée"
    )
    
    # Métadonnées
    date_creation = models.DateTimeField(auto_now_add=True)
    date_modification = models.DateTimeField(auto_now=True)
    date_acceptation = models.DateTimeField(null=True, blank=True)
    
    # Informations de session (pour analytics)
    ip_address = models.GenericIPAddressField(null=True, blank=True)
    user_agent = models.TextField(blank=True)
    
    class Meta:
        verbose_name = "Offre personnalisée IA"
        verbose_name_plural = "Offres personnalisées IA"
        ordering = ['-date_creation']
        indexes = [
            models.Index(fields=['statut', 'date_creation']),
            models.Index(fields=['utilisateur', 'date_creation']),
        ]
    
    def __str__(self):
        user_str = self.utilisateur.username if self.utilisateur else "Anonyme"
        return f"Offre {self.id} - {user_str} - {self.statut}"
    
    def accepter(self):
        """Marque l'offre comme acceptée"""
        from django.utils import timezone
        self.statut = 'acceptee'
        self.date_acceptation = timezone.now()
        self.save()
    
    def get_prestations_ids(self):
        """Retourne la liste des IDs de prestations recommandées"""
        return list(self.prestations.values_list('id', flat=True))


class OffrePrestation(models.Model):
    """
    Table intermédiaire entre Offre et Prestation
    Permet de stocker des infos supplémentaires sur chaque recommandation
    """
    
    offre = models.ForeignKey(
        OffrePersonnalisee,
        on_delete=models.CASCADE
    )
    
    prestation = models.ForeignKey(
        'Prestation',
        on_delete=models.CASCADE
    )
    
    # Informations de la recommandation
    priorite = models.IntegerField(
        default=1,
        help_text="Ordre de recommandation (1 = plus recommandé)"
    )
    
    raison_recommandation = models.TextField(
        help_text="Pourquoi cette prestation a été recommandée"
    )
    
    score_pertinence = models.FloatField(
        default=0.0,
        help_text="Score de pertinence calculé par l'IA (0-1)"
    )
    
    # Suivi
    consultee = models.BooleanField(
        default=False,
        help_text="L'utilisateur a-t-il consulté cette prestation ?"
    )
    
    date_consultation = models.DateTimeField(null=True, blank=True)
    
    class Meta:
        verbose_name = "Prestation dans l'offre"
        verbose_name_plural = "Prestations dans l'offre"
        ordering = ['priorite']
        unique_together = ['offre', 'prestation']
    
    def __str__(self):
        return f"{self.prestation.titre} (Priorité {self.priorite})"


class HistoriqueRecherche(models.Model):
    """
    Historique des recherches IA pour améliorer les recommandations
    """
    
    utilisateur = models.ForeignKey(
        User,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name='historique_recherches'
    )
    
    requete = models.TextField(help_text="Texte de la recherche")
    
    analyse = models.JSONField(
        encoder=DjangoJSONEncoder,
        help_text="Résultat de l'analyse IA"
    )
    
    nb_resultats = models.IntegerField(
        default=0,
        help_text="Nombre de prestations trouvées"
    )
    
    offre_generee = models.ForeignKey(
        OffrePersonnalisee,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name='recherches'
    )
    
    date_recherche = models.DateTimeField(auto_now_add=True)
    
    # Analytics
    duree_traitement_ms = models.IntegerField(
        null=True,
        blank=True,
        help_text="Temps de traitement en millisecondes"
    )
    
    class Meta:
        verbose_name = "Historique de recherche"
        verbose_name_plural = "Historiques de recherches"
        ordering = ['-date_recherche']
        indexes = [
            models.Index(fields=['utilisateur', 'date_recherche']),
        ]
    
    def __str__(self):
        return f"Recherche du {self.date_recherche.strftime('%d/%m/%Y %H:%M')}"


# ============================================================================
# MIGRATION À CRÉER
# ============================================================================

"""
Après avoir ajouté ces modèles, exécute :

python manage.py makemigrations
python manage.py migrate
"""