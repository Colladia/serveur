# Colladia

## URI :
- Diagram : `<address>/<diagram id>`
- Element : `<address>/<diagram id/<element id>[/<element id> ...]`

---

## Interface :
#### PUT :
- <addr>/<diagram> --> création d'un diagramme
- <addr>/<diagram>/<element> --> création d'un élément dans le diagramme
    - <addr>/<diagram>/<element>/<element> --> création d'un sous élément
    - input : properties=<properties as json>
    - output : {path:<path as json array>, type:PUT, properties:<properties as json>}

---

## Méthodes REST :
#### PUT :
- Création d'un nouveau diagramme :
    - Lancement d'un 'component' restlet ?
    - Création d'un nouveau conteneur de diagramme
- Création d'un nouvel élément

#### DEL :
- Suppression d'un élément/diagramme

#### GET :
- Renvoi la sérialisation JSON de l'élément / du diagramme
- Si on passe un timestamp, ne renvoi que les modifications qui ont eu lieu depuis

#### POST :
- Modification d'un ou plusieurs attributs d'un éléments (d'un diagramme ?)

---

## Agents :
#### Agent REST [MainCt] : 
- Liaison entre le SMA et le serveur restlet

#### Agent de sauvegarde [MainCt] :
- Sauvegarde les informations d'un diagramme sur demande dans un fichier
- Au lancement du serveur, restore les diagrammes à partir des fichiers

#### Agent de diagramme [DiaCt] :
- Gestion de l'état actuel du diagramme et de ses éléments

#### Agent horloge [DiaCt] :
- Gestion de l'horloge logique du diagramme

#### Agent historique [DiaCt] :
- Gestion de l'historique des modifications du diagramme
- Suppression des modifications au bout d'un certain temps ?
- Tableau cyclique (nombre de modifications conservées limité) ?

---

## Remarques :
- Chaque fois que le serveur renvoi des informations, il y ajoute le timestamp (horloge logique actuelle du serveur)
- Toutes les informations sur les éléments sont stockées sous forme de dictionnaires string->string
