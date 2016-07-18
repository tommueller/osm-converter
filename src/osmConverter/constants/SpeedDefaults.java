package osmConverter.constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import osmConverter.data.Link;
import osmConverter.data.MyArea;
import osmConverter.data.OsmNode;
import osmConverter.data.OsmPlace;
import osmConverter.data.OsmWay;
import osmConverter.io.IsLogging;
import osmConverter.logic.ConversionEventLogger;
import osmConverter.logic.CoordinateHelper;

/**
 * Speed defaults. Those are set using this list: http://wiki.openstreetmap.org
 * /wiki/OSM_tags_for_routing/Maxspeed#Implicit_maxspeeds
 * 
 * If no information concerning inside/outside city is available uses
 * default-values depending on street-type.
 * 
 * @author Tom Müller
 * 
 */
public class SpeedDefaults implements IsLogging {

    public static final int MOTORWAY = 130;
    public static final int MOTORWAY_LINK = 80;
    public static final int TRUNK_OUTSIDE = 130;
    public static final int TRUNK_INSIDE = 50;
    public static final int PRIMARY_OUTSIDE = 100;
    public static final int PRIMARY_INSIDE = 50;
    public static final int SECONDARY_OUTSIDE = 100;
    public static final int SECONDARY_INSIDE = 50;
    public static final int TERTIARY_OUTSIDE = 100;
    public static final int TERTIARY_INSIDE = 50;
    public static final int UNCLASSIFIED_OUTSIDE = 100;
    public static final int UNCLASSIFIED_INSIDE = 50;
    public static final int RESIDENTIAL_OUTSIDE = 100;
    public static final int RESIDENTIAL_INSIDE = 50;
    public static final int ROAD_INSIDE = 50;
    public static final int ROAD_OUTSIDE = 50;

    public static final int WALK_SPEED = 7;
    public static final int MODERAT_SPEED = 25;
    public static final int RURAL_SPEED = 100;

    public static final int DEFAULT_SPEED = 50;

    public static final int URBAN_SPEED = 50;
    public static final int NO_MAXSPEED = 1;

    public static final int TRUNK = 80;
    public static final int PRIMARY = 70;
    public static final int SECONDARY = 70;
    public static final int TERTIARY = 50;
    public static final int UNCLASSIFIED = 50;
    public static final int RESIDENTIAL = 40;
    public static final int ROAD = 30;

    private Logger logger;
    private Logger osmLogger;
    private ConversionEventLogger conversionLogger;

    private List<OsmPlace> places = new ArrayList<OsmPlace>();

    public SpeedDefaults(Map<String, OsmNode> nodes, String[] logs,
	    ConversionEventLogger cl) {
	super();

	logger = Logger.getLogger(logs[0]);
	osmLogger = Logger.getLogger(logs[2]);

	this.conversionLogger = cl;

	for (OsmNode node : nodes.values()) {
	    if (node.getTags().containsKey("place")) {
		places.add(new OsmPlace(node));
	    }
	}
    }

    public void checkForSpeedConflicts(OsmWay osmWay, Link link) {
	int speed = link.getSpeed();

	if (speed > 10
		&& osmWay.getTags().get("highway").equals("living_street")) {
	    logOsmWarning("Conflict in maxspeed (living_street)! Way-id: "
		    + osmWay.getId() + ", speed: " + speed);

	    conversionLogger.getScorer().reportSpeedTagConflict();
	}

	if (speed > 50 && osmWay.getTags().get("highway").equals("residential")) {
	    logOsmWarning("Conflict in maxspeed (residential)! Way-id: "
		    + osmWay.getId() + ", speed: " + speed);

	    conversionLogger.getScorer().reportSpeedTagConflict();
	}

	if (speed < 80 && osmWay.getTags().get("highway").equals("motorway")) {
	    logOsmWarning("Conflict in maxspeed (motorway)! Way-id: "
		    + osmWay.getId() + ", speed: " + speed);

	    conversionLogger.getScorer().reportSpeedTagConflict();
	}
    }

    public int getDefaultSpeed(int streetType, OsmWay osmWay,
	    Map<String, MyArea> myAreas, Map<String, OsmNode> nodes) {
	return getDefaultSpeed(streetType, osmWay, myAreas, nodes, false, false);
    }

    public int getDefaultSpeed(int streetType, OsmWay osmWay,
	    Map<String, MyArea> myAreas, Map<String, OsmNode> nodes,
	    boolean zoneUrban) {
	return getDefaultSpeed(streetType, osmWay, myAreas, nodes, true,
		zoneUrban);
    }

    private int getDefaultSpeed(int streetType, OsmWay osmWay,
	    Map<String, MyArea> myAreas, Map<String, OsmNode> nodes,
	    boolean zoneSet, boolean zoneUrban) {

	boolean isUrban = false;
	boolean dontKnow = false;

	if (zoneSet) {
	    isUrban = zoneUrban;
	} else if (streetType > 2) {
	    isUrban = true;
	} else {
	    dontKnow = true;
	}

	if (osmWay.getTags().containsKey("highway")) {
	    String highway = osmWay.getTags().get("highway");

	    if (highway.equals("motorway")) {
		return MOTORWAY;
	    } else if (highway.equals("motorway_link")) {
		return MOTORWAY_LINK;
	    } else if (highway.equals("trunk") || highway.equals("trunk_link")) {
		if (dontKnow) {
		    return TRUNK;
		} else if (isUrban) {
		    return TRUNK_INSIDE;
		} else {
		    return TRUNK_OUTSIDE;
		}
	    } else if (highway.equals("primary")
		    || highway.equals("primary_link")) {
		if (dontKnow) {
		    return PRIMARY;
		} else if (isUrban) {
		    return PRIMARY_INSIDE;
		} else {
		    return PRIMARY_OUTSIDE;
		}
	    } else if (highway.equals("secondary")
		    || highway.equals("secondary_link")) {
		if (dontKnow) {
		    return SECONDARY;
		} else if (isUrban) {
		    return SECONDARY_INSIDE;
		} else {
		    return SECONDARY_OUTSIDE;
		}
	    } else if (highway.equals("tertiary")) {
		if (dontKnow) {
		    return TERTIARY;
		} else if (isUrban) {
		    return TERTIARY_INSIDE;
		} else {
		    return TERTIARY_OUTSIDE;
		}
	    } else if (highway.equals("unclassified")) {
		if (dontKnow) {
		    return UNCLASSIFIED;
		} else if (isUrban) {
		    return UNCLASSIFIED_INSIDE;
		} else {
		    return UNCLASSIFIED_OUTSIDE;
		}
	    } else if (highway.equals("residential")) {
		if (dontKnow) {
		    return RESIDENTIAL;
		} else if (isUrban) {
		    return RESIDENTIAL_INSIDE;
		} else {
		    return RESIDENTIAL_OUTSIDE;
		}
	    } else if (highway.equals("road")) {
		if (dontKnow) {
		    return ROAD;
		} else if (isUrban) {
		    return ROAD_INSIDE;
		} else {
		    return ROAD_OUTSIDE;
		}
	    } else if (highway.equals("living_street")) {
		return WALK_SPEED;
	    } else if (highway.equals("service")) {
		return MODERAT_SPEED;
	    } else if (highway.equals("DE:motorway")) {
		return MOTORWAY;
	    } else if (highway.equals("ford")) {
		return MODERAT_SPEED;
	    } else {
		return DEFAULT_SPEED;
	    }
	} else {
	    return 40;
	}
    }

    private boolean isInsideArea(OsmWay osmWay, Map<String, MyArea> myAreas) {
	for (MyArea area : myAreas.values()) {
	    if (area.getType().equals("town")) {
		for (OsmNode node : osmWay.getNodes()) {
		    if (area.contains(node.getLon(), node.getLat())) {
			return true;
		    }
		}
	    }
	}
	return false;
    }

    /**
     * Checks if there is a place (city, suburb etc.) closer than the specified
     * radius for that kind of a place
     * 
     * @param osmWay
     * @return
     */
    private boolean isPlacePOINearby(OsmWay osmWay) {
	for (OsmPlace place : places) {
	    double distance = 0;
	    for (OsmNode wayNode : osmWay.getNodes()) {
		distance = CoordinateHelper.calcDistanceBetweenNodes(wayNode,
			place.getNode());
	    }

	    if (distance <= place.getRadius()) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public void logError(String message) {
	logger.log(Level.SEVERE, message);
    }

    @Override
    public void logInfo(String message) {
	logger.log(Level.INFO, message);
    }

    private void logOsmWarning(String message) {
	osmLogger.log(Level.SEVERE, message);
    }

    @Override
    public void logWarning(String message) {
	logger.log(Level.WARNING, message);
    }

    private boolean wayIsUrban(OsmWay osmWay, Map<String, MyArea> myAreas,
	    Map<String, OsmNode> nodes) {
	// http://wiki.openstreetmap.org/wiki/OSM_tags_for_routing#City
	// doing the 3 necesarry test
	if (osmWay.getTags().containsKey("is_in")) {
	    return true;
	} else if (isInsideArea(osmWay, myAreas)) {
	    return true;
	} else if (isPlacePOINearby(osmWay)) {
	    return true;
	}
	return false;
    }

}
