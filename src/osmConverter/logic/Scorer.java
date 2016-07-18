package osmConverter.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import osmConverter.data.OsmWay;
import osmConverter.io.IsLogging;

public class Scorer implements IsLogging {

    public final static String DEF = "Default";
    public final static String EXPLICIT = "Explicit";
    public final static String NAN = "Not a number";

    private final HashMap<String, HashMap<String, Double>> highwaySpeeds = new HashMap<String, HashMap<String, Double>>();
    private final HashMap<String, Double> lanes = new HashMap<String, Double>();

    Logger logger;

    private final HashMap<String, HashMap<String, Integer>> namedHighways = new HashMap<String, HashMap<String, Integer>>();

    private final HashMap<String, Double> noLanes = new HashMap<String, Double>();

    private int speedConflicts = 0;
    private final Map<String, Integer> unnamedHighways = new HashMap<String, Integer>();

    public Scorer(String logName) {
	logger = Logger.getLogger(logName);
    }

    public HashMap<String, HashMap<String, Double>> getHighwaySpeeds() {
	return highwaySpeeds;
    }

    public HashMap<String, Double> getLanes() {
	return lanes;
    }

    public HashMap<String, HashMap<String, Integer>> getNamedHighways() {
	return namedHighways;
    }

    public HashMap<String, Double> getNoLanes() {
	return noLanes;
    }

    public Map<String, Integer> getUnnamedHighways() {
	return unnamedHighways;
    }

    @Override
    public void logError(String message) {
	logger.log(Level.SEVERE, message);
    }

    @Override
    public void logInfo(String message) {
	logger.log(Level.INFO, message);
    }

    @Override
    public void logWarning(String message) {
	logger.log(Level.WARNING, message);
    }

    /**
     * Report a missing maxspeed-tag.
     * 
     * @param osmWay
     *            The osmWay missing the maxspeed-Tag.
     */
    public void reportDefaultMaxSpeed(OsmWay osmWay) {
	String highway = osmWay.getTags().get("highway");

	if (highwaySpeeds.containsKey(highway)) {

	    HashMap<String, Double> temp = highwaySpeeds.get(highway);
	    if (temp.containsKey(DEF)) {
		double tempCount = temp.get(DEF);
		tempCount++;
		temp.put(DEF, tempCount);
		highwaySpeeds.put(highway, temp);
	    } else {
		temp.put(DEF, 1.);
		highwaySpeeds.put(highway, temp);
	    }

	} else {
	    HashMap<String, Double> newHighway = new HashMap<String, Double>();
	    newHighway.put(DEF, 1.);
	    highwaySpeeds.put(highway, newHighway);
	}
    }

    /**
     * Report a street with an existing lane-tag!
     * 
     * @param osmWay
     *            The osmWay containing the lane-tag.
     */
    public void reportExistingLaneTag(OsmWay osmWay) {
	if (lanes.containsKey(osmWay.getTags().get("highway"))) {
	    double temp = lanes.get(osmWay.getTags().get("highway"));
	    temp++;
	    lanes.put(osmWay.getTags().get("highway"), temp);
	} else {
	    lanes.put(osmWay.getTags().get("highway"), 1.);
	}
    }

    /**
     * Report a maxspeed-tag with a number as value.
     * 
     * @param osmWay
     *            The osmWay containing the maxspeed-tag.
     */
    public void reportExplicitMaxSpeed(OsmWay osmWay) {

	String highway = osmWay.getTags().get("highway");

	if (highwaySpeeds.containsKey(highway)) {

	    HashMap<String, Double> temp = highwaySpeeds.get(highway);
	    if (temp.containsKey(EXPLICIT)) {
		double tempCount = temp.get(EXPLICIT);
		tempCount++;
		temp.put(EXPLICIT, tempCount);
		highwaySpeeds.put(highway, temp);
	    } else {
		temp.put(EXPLICIT, 1.);
		highwaySpeeds.put(highway, temp);
	    }
	} else {
	    HashMap<String, Double> newHighway = new HashMap<String, Double>();
	    newHighway.put(EXPLICIT, 1.);
	    highwaySpeeds.put(highway, newHighway);
	}
    }

    /**
     * Report a street with an existing name tag, OR with an existing
     * "this street does not have a name"-tag!
     * 
     * @param nameTag
     *            The type of the name tag (e.g. name, ref, loc_name ...)
     * @param highway
     *            The highway-type of the street.
     */
    public void reportNamedStreet(String nameTag, String highway) {
	if (namedHighways.containsKey(highway)) {
	    HashMap<String, Integer> temp = namedHighways.get(highway);
	    if (temp.containsKey(nameTag)) {
		int tempCount = temp.get(nameTag);
		tempCount++;
		temp.put(nameTag, tempCount);
		namedHighways.put(highway, temp);
	    } else {
		temp.put(nameTag, 1);
		namedHighways.put(highway, temp);
	    }
	} else {
	    HashMap<String, Integer> newHighway = new HashMap<String, Integer>();
	    newHighway.put(nameTag, 1);
	    namedHighways.put(highway, newHighway);
	}
    }

    /**
     * Report a maxspeed-tag which does not contain a number, but a logical
     * expression.
     * 
     * @param osmWay
     */
    public void reportNANMaxSpeed(OsmWay osmWay) {
	String highway = osmWay.getTags().get("highway");

	if (highwaySpeeds.containsKey(highway)) {

	    HashMap<String, Double> temp = highwaySpeeds.get(highway);
	    if (temp.containsKey(NAN)) {
		double tempCount = temp.get(NAN);
		tempCount++;
		temp.put(NAN, tempCount);
		highwaySpeeds.put(highway, temp);
	    } else {
		temp.put(NAN, 1.);
		highwaySpeeds.put(highway, temp);
	    }
	} else {
	    HashMap<String, Double> newHighway = new HashMap<String, Double>();
	    newHighway.put(NAN, 1.);
	    highwaySpeeds.put(highway, newHighway);
	}
    }

    /**
     * Report an absent lanes-tag!
     * 
     * @param osmWay
     *            The osmWay not containing a lanes-tag.
     */
    public void reportNotExistingLaneTag(OsmWay osmWay) {
	if (noLanes.containsKey(osmWay.getTags().get("highway"))) {
	    double temp = noLanes.get(osmWay.getTags().get("highway"));
	    temp++;
	    noLanes.put(osmWay.getTags().get("highway"), temp);
	} else {
	    noLanes.put(osmWay.getTags().get("highway"), 1.);
	}
    }

    /**
     * Report a conflict between the speed that is set and the highway-type.
     */
    public void reportSpeedTagConflict() {
	this.speedConflicts++;
    }

    /**
     * Report a street with a _missing_ name Tag!
     * 
     * @param highway
     *            The highway-type of the street.
     */
    public void reportUnnamedStreet(String highway) {

	if (unnamedHighways.containsKey(highway)) {
	    int temp = unnamedHighways.get(highway);
	    temp++;
	    unnamedHighways.put(highway, temp);
	} else {
	    unnamedHighways.put(highway, 1);
	}

    }

    /**
     * Report a lane-Tag which has value that is not usable.
     * 
     * @param osmWay
     *            The osmWay containing the wrong tag.
     */
    public void reportWrongLaneTag(OsmWay osmWay) {
	if (noLanes.containsKey(osmWay.getTags().get("highway"))) {
	    double temp = noLanes.get(osmWay.getTags().get("highway"));
	    temp++;
	    noLanes.put(osmWay.getTags().get("highway"), temp);
	} else {
	    noLanes.put(osmWay.getTags().get("highway"), 1.);
	}
    }
}
