from django.db import models
from django.contrib.auth.models import User
from django.core.validators import MinValueValidator, MaxValueValidator


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