import site
from django.contrib import admin
from .models import *



# Register your models here.
admin.site.register(Prestation)
admin.site.register(Categorie)
admin.site.register(Tag)
admin.site.register(Image)
admin.site.register(Avis)


