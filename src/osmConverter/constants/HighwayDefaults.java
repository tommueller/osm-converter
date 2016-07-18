package osmConverter.constants;

import java.util.HashMap;
import java.util.Map;


/**
 * Class providing functionality to convert streetcategory to streettype-id.
 * @author Tom Müller
 *
 */
public class HighwayDefaults {

    private Map<String, Integer> highwayDefaults = new HashMap<String, Integer>();

    public HighwayDefaults() {
	this.setHighwayDefaults();
    }

    private void setHighwayDefaults() {
	highwayDefaults.put("motorway", 0);
	highwayDefaults.put("motorway_link", 0);

	highwayDefaults.put("trunk", 1);
	highwayDefaults.put("trunk_link", 1);
	highwayDefaults.put("primary", 1);
	highwayDefaults.put("primary_link", 1);
	
	highwayDefaults.put("secondary", 2);
	highwayDefaults.put("secondary_link", 2);
	highwayDefaults.put("tertiary", 2);

	highwayDefaults.put("residential", 3);
	highwayDefaults.put("unclassified", 3);
	
	highwayDefaults.put("living_street", 4);
	
	/* Zugangsstraßen, hoher Teil unbennant! */
	highwayDefaults.put("service", 5);

	/* following are not too clear ... */
	highwayDefaults.put("pedestrian", 10); /* Fußgängerweg */
	highwayDefaults.put("cycleway", 10); /* Fahrradweg */
	highwayDefaults.put("footway", 10); /* Fußweg */
	highwayDefaults.put("bridleway", 10); /* Reitweg */
	highwayDefaults.put("path", 10); /* Weg */
	highwayDefaults.put("track", 10); /* Landwirtschaftliche Wege */
	highwayDefaults.put("steps", 10); /* Treppen */
	highwayDefaults.put("platform", 10); /* Bushaltestelle */
	/* Bus-Eigenes-Schienenartiges Weg-Dingsi */
	highwayDefaults.put("bus_guideway", 10);
	highwayDefaults.put("ford", 10); /* Wasser über der Straße */
	highwayDefaults.put("sidewalk", 10); /* Bürgersteig */
	highwayDefaults.put("escalator", 10); /* Rolltreppe */
	highwayDefaults.put("elevator", 10); /* Aufzug */
	highwayDefaults.put("crossing", 10); /* Zebrastreifen */
	highwayDefaults.put("bus_stop", 10); /* Bushaltestelle */
	highwayDefaults.put("traffic_signals", 10); /* Ampel */
	highwayDefaults.put("unsurfaced", 10); /* ungeteert */

	highwayDefaults.put("road", 10); /* means WIP */
    }
    

    /**
     * 
     * Converts street-category to streettype-id.
     * 
     * @param osmTag The highway-tag to be converted.
     * @return The streettype-id, 11 if tag is unknown.
     */
    public int getStreetCategory(String osmTag) {
	if (highwayDefaults.containsKey(osmTag)) {
	    return highwayDefaults.get(osmTag);
	} else {
	    return 11;
	}
    }
}
