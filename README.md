# Colladia

## Interface REST :

#### Général :
- entrées/sorties communes à tous les requêtes/modifications
- output :
    - `status` : état de la requête (`KO` ou `OK`)
    - `error` : message d'erreur si `status=KO`
    - `type` : type de la requête initiale (`PUT`, `GET`, `POST` ou `DELETE`)
    - `clock` : horloge logique de la dernière modification du diagramme
- input :
    - `last-clock` : dernière horloge logique reçue par le client ayant envoyé la requête (optionnel)

#### Remarques :
- les sorties présentées ci-dessous sont celles des modifications apportées au diagramme et pas le retour de la requête REST
- le retour de la requête REST contient un champ `status`, un champ `clock` et :
    - soit un champ `description` contenant la description complète du diagramme et de ses éléments si `last-clock` n'a pas été spécifié dans la requête ou si l'historique ne peut pas retourner la liste de modification désirée
    - soit un champ `modification-list` contenant un tableau JSON de modifications (telles que décrites ci-après)
- Chaque requête `PUT`, `POST` ou `DELETE` incrémente l'horloge logique du diagramme ciblé de 1
- Les requêtes `GET` n'induisent aucune modification de la valeur de l'horloge

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
- suppression récursive d'un diagramme ou d'un élément :
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

## Description :
#### Structure générale :
- MainCt : conteneur principal
    - agents standards JADE
    - 1 couple RestAgt + RestServer
    - 1 SaveAgt
- DiaCt : conteneur pour les agents des diagrammes
    - 1 DiaAgt par diagramme
    - 1 ClockAgt par diagramme
    - 1 HistAgt par diagramme

#### RestAgt + RestServer :
- chargé de récupérer et de traiter les requêtes REST
    - les requêtes sont ensuite transférées sous forme d'ACLMessage au DiaAgt correspondant si besoin
- création des nouveaux diagrammes (DiaAgt + ClockAgt + HistAgt)
- liste des diagrammes disponibles

###### Comportements :
- RestServer :
    - != comportement JADE
    - attente d'une requête client puis :
        - création d'un diagramme ou liste des diagrammes disponibles
        - ou envoie d'un message vers le DiaAgt correspondant pour exécution de la requête
            - le message est envoyé avec un champ `reply-to` indiquant le ClockAgt du diagramme
- ReceiveBhv :
    - après envoi d'une requête à un DiaAgt, attente de la réponse
    - après réception de la réponse et traitement, le résultat est envoyé au client REST

#### DiaAgt :
- stocke l'état courant d'un diagramme
- pour l'instant, les différents éléments, sous-éléments, etc. sont stockés dans une structure récursive
- implémente les fonctions de recherche, ajout, suppression, modification etc. d'éléments

###### Comportements :
- ReceiveBhv :
    - attend une requête du RestAgt, la traite et envoi une réponse

#### EltAgt :
- différent de DiaAgt ?
- TODO

#### SaveAgt :
- agent responsable de la sauvegarde et de la restauration des diagrammes du serveur après redémarrage
- chaque diagramme est sauvegardé dans un fichier au format JSON

###### Comportements :
- RestoreBhv :
    - comportement one-shot lancé au démarrage de l'agent
    - à partir des fichiers sauvegardés, créer les différents diagrammes (DiaAgt + ClockAgt + HistAgt) et envoie un message au nouveau DiaAgt pour restaurer la valeur des différents éléments
    - une fois terminé, lance le TickerBhv et le ReceiveBhv
- TickerBhv :
    - comportement qui envoie régulièrement un message à tous les DiaAgt leur demandant de communiquer leur description complète
- ReceiveBhv :
    - comportement cyclique chargé de récupérer les réponses des DiaAgt aux messages envoyé par le TickerBhv
    - à la réception de la description d'un diagramme, écrit cette dernière dans un fichier .json

#### ClockAgt :
- agent chargé de gérer l'horloge logique d'un diagramme

###### Comportements :
- ReceiveBhv :
    - comportement cyclique qui, à la réception d'un message INFORM (venant du DiaAgt) :
        - incrémente l'horloge logique si le type de la requête initiale était `PUT`, `POST` ou `DELETE`
        - ajoute un champ `clock` au message qui contient la valeur courante de l'horloge du diagramme
        - envoie ensuite le message au HistAgt du diagramme

#### HistAgt :
- agent chargé de stocker l'historique des modifications du diagramme

###### Comportements :
- ReceiveBhv :
    - comportement cyclique qui, à la réception d'un message INFORM (venant du ClockAgt) :
        - si le type de la requête initiale était `PUT`, `POST` ou `DELETE`, ajoute le contenu du message à la liste des modifications
        - si le message contient un champ `last-clock` et que sa valeur est supérieure à l'horloge la première modification encore stockée dans la liste des modifications, renvoie la liste des modifications appliquée depuis celle portant l'horloge `last-clock`
        - sinon envoie un message au DiaAgt pour récupérer la description complète du diagramme en ajoutant un champ `reply-to` vers le RestAgt

---

## Messages :
#### Général :
- content : dictionnaire JSON sérialisé
    - les champs décrits ci-dessous n'appartenant pas au format ACL sont des champs de ce dictionnaire
- conversation-id : id unique généré pour chaque requête REST

#### RestAgt :
- performatif : `REQUEST`
- destinataires : DiaAgt
- reply-to : ClockAgt
- type : `PUT`, `GET`, `DELETE` ou `POST` dépendant du type de la requête REST initiale
- path : chemin du diagramme/élément visé par la requête
- properties : liste des propriétés et de leurs valeurs dans le cas d'une modification/création d'un élément
- properties-list : liste des propriétés à supprimer au sein d'un élément
- last-clock : dernière horloge reçue par le client si spécifiée dans la requête REST

#### DiaAgt :
###### Succès :
- performatif : `INFORM`
- status : `OK`
- destinataires : valeur du champ `reply-to`, ou `sender` si le premier est inexistant
- le contenu du message est celui de la requête, plus les éventuels champs suivants :
    - description : description récursive des propriétés et éléments d'un diagramme/élément

###### Erreur :
- performatif : `FAILURE`
- status : `KO`
- destinataires : valeur du champ `sender` (le `reply-to` est ignoré)
- error : message d'erreur

#### SaveAgt :
###### Au démarrage (restoration):
- performatif : `REQUEST`
- destinataires : tous les DiaAgt (une fois créés)
- type : `RESTORE`
- description : description recursive des propriétés du diagramme et de ses éléments

###### À chaque tick :
- performatif : `REQUEST`
- destinataires : tous les DiaAgt
- type : `GET`
- path : `[]` (liste vide --> retourne la description complète du diagramme)

#### ClockAgt :
- performatif : `INFORM`
- destinataires : HistAgt
- le contenu du message est le même que celui du message reçu plus un champ `clock` contenant la valeur de l'horloge logique du diagramme

#### HistAgt
###### --> RestAgt :
- performatif : `INFORM`
- destinataires : RestAgt
- status : `OK`
- clock : valeur de l'horloge logique insérée dans le contenu du message par le ClockAgt
- modification-list : liste des modifications appliquées au diagramme depuis l'horloge `last-clock` spécifiée par le client

###### --> DiaAgt :
- performatif : `INFORM`
- destinataires : DiaAgt
- reply-to : RestAgt
- type : `GET`
- path : `[]` (liste vide --> retourne la description complète du diagramme)
- clock : valeur de l'horloge logique insérée dans le contenu du message par le ClockAgt
