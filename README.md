# Colladia

## Interface REST :

#### Général :
- entrées/sorties communes à tous les requêtes/modifications
- output :
    - `status` : code indiquant le statut de la requête (cf ci-dessous)
    - `error` : message d'erreur si `status=KO`
    - `type` : type de la requête initiale (`PUT`, `GET`, `POST` ou `DELETE`)
    - `clock` : horloge logique de la dernière modification du diagramme
- input :
    - `last-clock` : dernière horloge logique reçue par le client ayant envoyé la requête (optionnel)
    
#### Code de statut :
- succès (`2xx`) :
    - `200` : OK
- redirection (`3xx`) :
    - `304` : non modifié
- erreur client (`4xx`) :
    - `400` : requête mal formée
    - `401` : existe déjà
    - `404` : non trouvé
- erreur serveur (`5xx`) :
    - `500` : erreur interne

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
    - 1+ EltAgt par diagramme (au moins un elément représentant le diagramme)
    - 1 ClockAgt par diagramme
    - 1 HistAgt par diagramme

#### RestAgt + RestServer :
- chargé de récupérer et de traiter les requêtes REST
    - les requêtes sont ensuite transférées sous forme d'ACLMessage au EltAgt racine correspondant si besoin
- création des nouveaux diagrammes (EltAgt racine + ClockAgt + HistAgt)
- liste des diagrammes disponibles

###### Comportements :
- RestServer :
    - != comportement JADE
    - attente d'une requête client puis :
        - création d'un diagramme ou liste des diagrammes disponibles
        - ou envoie d'un message vers le EltAgt racine correspondant pour exécution de la requête
            - le message est envoyé avec un champ `reply-to` indiquant le ClockAgt du diagramme
- ReceiveBhv :
    - après envoi d'une requête à un EltAgt racine, attente de la réponse
    - après réception de la réponse et traitement, le résultat est envoyé au client REST

#### EltAgt :
- stocke l'état courant d'un élément du diagramme ou du diagramme lui-même
- pour chaque diagramme, les EltAgt forment une arborescence dont la racine est le EltAgt s'occupant du diagramme
- si à sa création l'EltAgt n'a pas de parent (racine), il s'enregistre auprès du DF
- implémente les fonctions de recherche, ajout, suppression, modification etc. d'éléments sur l'arborescence

###### Comportements :
- ReceiveBhv :
    - attend une requête du RestAgt ou d'un EltAgt parent
    - execute les opérations nécéssaires à la réalisation de la requête (transfert à un/plusieurs fils, instructions locales etc.)
    - retourne éventuellement une réponse à l'expéditeur de la requête initiale (requête REST)
    - PUT :
        - si l'élément courant est l'avant-dernier élément de l'uri :
            - ajoute un nouvel élément si celui-ci n'existe pas déjà (création d'un EltAgt et ajout à l'arborescence)
        - sinon :
            - transfert du message au fils correspondant au prochain élément dans l'uri si il existe ou retourne une erreur
    - GET :
        - si l'élément courant a une profondeur dans l'arborescence strictement inférieure à celle de la cible de l'uri :
            - transfert du message au fils correspondant au prochain élément dans l'uri si il existe ou retourne une erreur
        - sinon :
            - si l'élément est une feuille :
                - répond au message en envoyant la liste des propriétés et leurs valeures
            - sinon :
                - transfert le message à tous les sous-éléments et lance un behaviour WaitDescription pour attendre les réponses
    - DELETE :
        - si la requête contient un champ `properties-list` (suppression de propriété) :
            - si l'élément courant est le dernier élément de l'uri :
                - suppression des propriétés dans l'élément si elles existent
            - sinon :
                - transfert du message au fils correspondant au prochain élément dans l'uri si il existe ou retourne une erreur
        - sinon (suppression d'élément) :
            - si l'élément courant a une profondeur dans l'arborescence strictement inférieure à celle de la cible de l'uri :
                - transfert du message au fils correspondant au prochain élément dans l'uri si il existe ou retourne une erreur
            - sinon :
                - si l'élément courant est le dernier élément de l'uri :
                    - envoie une réponse à l'expéditeur initiale de la requête
                - transfert du message à tous les fils
                - autodestruction
    - POST :
        - si l'élément courant est le dernier élément de l'uri :
            - modification des propriétés de l'élément
            - envoie une réponse à l'expéditeur initiale de la requête
        - sinon :
            - transfert du message au fils correspondant au prochain élément dans l'uri si il existe ou retourne une erreur
- WaitDescription :
    - après avoir envoyé un message à chaque fils pour obtenir leur description, attend les retours
    - une fois une réponse à chaque message reçu, formule une réponse à l'agent ayant envoyé la requête (EltAgt parent ou ClockAgt à cause du `reply-to`)

#### SaveAgt :
- agent responsable de la sauvegarde et de la restauration des diagrammes du serveur après redémarrage
- chaque diagramme est sauvegardé dans un fichier au format JSON

###### Comportements :
- RestoreBhv :
    - comportement one-shot lancé au démarrage de l'agent
    - à partir des fichiers sauvegardés, créé les différents diagrammes (EltAgt racine + ClockAgt + HistAgt) et envoie un message au nouveau EltAgt racine pour restaurer la valeur de ses différents sous-éléments (type RESTORE)
    - une fois terminé, lance le TickerBhv et le ReceiveBhv
- TickerBhv :
    - comportement qui envoie régulièrement un message à tous les EltAgt racine leur demandant de communiquer leur description complète
- ReceiveBhv :
    - comportement cyclique chargé de récupérer les réponses des EltAgt aux messages envoyé par le TickerBhv
    - à la réception de la description d'un diagramme, écrit cette dernière dans un fichier .json

#### ClockAgt :
- agent chargé de gérer l'horloge logique d'un diagramme

###### Comportements :
- ReceiveBhv :
    - comportement cyclique qui, à la réception d'un message INFORM (venant d'un EltAgt) :
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
        - sinon envoie un message au EltAgt racine pour récupérer la description complète du diagramme en ajoutant un champ `reply-to` vers le RestAgt

---

## Messages :
#### Général :
- content : dictionnaire JSON sérialisé
    - les champs décrits ci-dessous n'appartenant pas au format ACL sont des champs de ce dictionnaire
- conversation-id : id unique généré pour chaque requête REST et conservé durant tout le traitement de cette dernière

#### RestAgt :
- performatif : `REQUEST`
- destinataires : EltAgt racine
- reply-to : ClockAgt
- type : `PUT`, `GET`, `DELETE` ou `POST` dépendant du type de la requête REST initiale
- path : chemin du diagramme/élément visé par la requête
- properties : liste des propriétés et de leurs valeurs dans le cas d'une modification/création d'un élément
- properties-list : liste des propriétés à supprimer au sein d'un élément pour certaines requêtes `DELETE`
- last-clock : dernière horloge reçue par le client si spécifiée dans la requête REST

#### EltAgt :
###### --> EltAgt :
- Les messages envoyés d'un EltAgt vers un autre EltAgt ne sont que des transferts d'une requête initiale reçue.
- Tous les champs du messages et du contenu sont convervés (incluant le `sender` et le `reply-to`)

###### Succès :
- performatif : `INFORM`
- status : `200`
- destinataires : valeur du champ `reply-to`, ou `sender` si le premier est inexistant
- le contenu du message est celui de la requête, plus les éventuels champs suivants :
    - description : description récursive des propriétés et éléments d'un diagramme/élément

###### Erreur :
- performatif : `FAILURE`
- status : un code d'erreur (`3xx`, `4xx` ou `5xx`)
- destinataires : valeur du champ `sender` (le `reply-to` est ignoré)
- error : message d'erreur

#### SaveAgt :
###### Au démarrage (restoration):
- performatif : `REQUEST`
- destinataires : tous les EltAgt racine (une fois créés)
- type : `RESTORE`
- description : description recursive des propriétés du diagramme et de ses éléments

###### À chaque tick :
- performatif : `REQUEST`
- destinataires : tous les EltAgt racine
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
- status : `200`
- clock : valeur de l'horloge logique insérée dans le contenu du message par le ClockAgt
- modification-list : liste des modifications appliquées au diagramme depuis l'horloge `last-clock` spécifiée par le client

###### --> EltAgt racine :
- performatif : `INFORM`
- destinataires : EltAgt racine
- reply-to : RestAgt
- type : `GET`
- path : `[]` (liste vide --> retourne la description complète du diagramme)
- clock : valeur de l'horloge logique insérée dans le contenu du message par le ClockAgt
