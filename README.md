# osm-converter
Read / Convert / Analyse OSM Data

Die Software OSMConverter entstand im Zuge der Bachelorarbeit von Tom Müller mit dem Titel
**Evaluierung der Nutzbarkeit von OpenStreetMap Daten für Intelligent Transport Systems (ITS)**

###Bedienung:

Die OSMConverter.jar _muss_ sich mit der zu konvertierenden name.osm-Datei in einem Verzeichnis befinden!

Aufruf erfolgt dann in der Kommandozeile über:
```bash
java -jar OSMConverter -file name.osm
```

In diesem Falle startet der OSMConverter und legt den Ordner "Output" an.
In diesem befinden sich die 3 relevanten Log-Dateien.

Optional können noch folgende Parameter verwand werden:
```bash
-writeStats 	(erzeugt Diagramme und .csv-Dateien zu maxspeed-, name- und lanes-Tag)
				 
-simpleMap		(erzeugt die Berechnung der vereinfachten Karte, Ausgabe erfolgt nur wenn zusätzlich -writeShapes benutzt wird!)
		
-writeShapes	(erzeugt eine shape-datei mit einigen Informationen; falls -simpleMap genutzt wird, erstellt es zusätzlich die simpleMap als shape-file)

-writeTomFiles 	(erzeugt die TOM-File-Dateien mit allen gesetzten Attributen)
```
