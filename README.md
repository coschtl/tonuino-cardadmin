# tonuino-cardadmin

Eine einfache Applikation f�r die Verwaltung von [Tonuino](https://www.voss.earth/tonuino/) SD-Karten.

F�r Nicht-Entwickler existieren precompiled Versionen unter */bin*


Das Programm ist in Java-Swing entwickelt und daher theoretisch �berall lauff�hig. Allerdings mit ein paar Einschr�nkungen (siehe unten).
F�r Windows-User gibt es eine exe (ein installiertes Java 8 wird ben�tigt): [https://github.com/coschtl/tonuino-cardadmin/blob/main/TonuinoCardAdmin.exe](https://github.com/coschtl/tonuino-cardadmin/blob/main/TonuinoCardAdmin.exe)

![](screenshot.png)

## Folgende Funktionen habe ich umgesetzt:

* Karten k�nnen sowohl direkt als auch �offline� in einem Ordner im Filesystem verwaltet werden
* Analyse des Karten-Inhalt mit Vorschl�gen zu �nderungen
* Erstellen neuer Ordner auf der Karte
* Hinzuf�gen von Files
* Anzeige der ID-Tags der Files mit der M�glichkeit zum Editieren
* Abspielen der Files
* Neu-Sortierung der Files eines Ordners
* Normalisieren aller Files eines Ordners (Windows only, l�uft per Shell-Aufruf, also ev. portierbar)
* Generierung eines Inhaltsverzeichnisses der Karte

## Was dem Programm noch fehlt:

* Code-�berarbeitung (es musste schnell gehen)
* Bessere Fehlerbehandlung

## Konfiguration

Das Konfigurationsfile "configuration.properties" wird im Startverzeichnis der exe gesucht.
Alternativ kann auch ein Environment-Property "cardAdminConfigFile" gesetzt werden, das den Pfad des Konfig-Files enth�lt.

Folgende Konfigurationsm�glichkeiten gibt es:

```
# normalizing program
# if not set, no normalizing can be done
mp3.normalizing.commandline=C:\\Program Files (x86)\\MP3Gain\\mp3gain.exe
mp3.normalizing.options = /a /k

# alternative card-roots
# i.e. for using a local folder as card-backup
alternative.card.root = D:/media/Tonuino/SD-Karte
 
# default content root
default.content.root = D:/media/Tonuino/Content

# index file location
card-index.location = D:/media/Tonuino/SD-Karten-Index
# set to csv to generate csv files
# any other string (or no value) will generate human readable files
card-index.format = csv
```