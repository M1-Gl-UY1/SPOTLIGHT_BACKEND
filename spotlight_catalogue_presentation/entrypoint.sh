#!/bin/sh

echo "📌 Appliquer les migrations…"
python manage.py migrate

echo "📌 Collecte des fichiers statiques…"
python manage.py collectstatic --noinput

echo "🚀 Lancement du serveur Django…"
python manage.py runserver 0.0.0.0:8000
