#Beschreibung TOM-File-Format

Separator ist ein ```tab```
Alle Koordinaten sind auf ints multipliziert.

Zunächst alle Kanten im folgenden Format:
```ID	StreetType	Maxspeed	Name	Startknoten-ID	startLat	startLon	Endknoten-ID	endLat endLon	Länge	Befahrerlaubnis		Einbahnstraße		Anzahl Folgekanten		Anzahl Vorgängerkanten		n-Folgekanten		k-Vorgängerkanten```

Durch ### nodes ### getrennt folgen die Knoten:
```ID	lat		lon```
