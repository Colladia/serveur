# Colladia

## Interface :

#### Global :
- `status` --> état de la requête (`KO` ou `OK`)
- `error` --> message d'erreur si `status=KO`
- `type` --> type de la requête initiale (`PUT`, `GET`, `POST` ou `DELETE`)

#### PUT :
- création d'un diagramme :
    - uri : `<addr>/<diagram>`
    - output : `{path:<diagram name in json array>}`
- création d'un sous-élément dans un diagramme/élément :
    - uri : `<addr>/<diagram>/[<element> ...]/<element>`
    - input : `properties=<properties as json map>`
    - output : `{path:<path as json array>, properties:<properties as json map>}`
    
#### GET :
- liste des diagrammes disponibles :
    - uri : `<adrr>`
    - output : `{list:<diagram list as json>}`
- récupération de la description (propriétés et descriptions des sous-éléments) d'un diagramme/élément
    - uri : `<adrr>/<diagram>[/<element> ...]`
    - output : `{path:<path as json array>, description:<properties and sub elements as json map>}`
    
#### DELETE :
- suppression recursive d'un diagramme ou d'un élément :
    - uri : `<adrr>/<diagram>[/<element> ...]`
    - output : `{path:<path as json array>}`
- suppression de propriétés :
    - uri : `<adrr>/<diagram>[/<element> ...]`
    - input : `properties-list=<properties to remove as json array>`
    - output : `{path:<path as json array>, properties-list:<properties removed as json array>}`
    
#### POST :
- modification/ajout de propriétés à un élément :
    - uri : `<adrr>/<diagram>[/<element> ...]`
    - input : `properties=<properties to add/modify as json map>`
    - output : `{path:<path as json array>, properties:<properties added/modified as json map>}`

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
