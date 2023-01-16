# osm-converter
Read / Convert / Analyse OSM Data

Die Software OSMConverter entstand im Zuge der Bachelorarbeit von Tom Müller mit dem Titel
**Evaluierung der Nutzbarkeit von OpenStreetMap Daten für Intelligent Transport Systems (ITS)**

[Die Arbeit kann hier heruntergeladen werden](http://newnoise.peacock.uberspace.de/bachelor/Bachelorarbeit_Tom_M%C3%BCller_Evaluierung_von_OSM_Daten.pdf
)
### Bedienung:

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

### Verwendung des Quellcodes
Um aus den Quelldateien ein lauffähiges Projekt zu erstellen, werden folgende Abhängigkeiten benötigt:
+ [gnujaxp.jar](https://www.gnu.org/software/classpathx/jaxp/)
+ [iText v2.1.5](https://github.com/itext/itextpdf)
+ [jFreeChart](http://www.jfree.org/jfreechart/) - jcommon.jar, jfreechart.jar, jfreechart-experimental.jar, jfreechart-swt.jar
+ [JTS v1.7.1](http://www.vividsolutions.com/jts/JTSHome.htm)
+ [jUnit](http://junit.org/junit4/)
+ [geotools](http://www.geotools.org/)
