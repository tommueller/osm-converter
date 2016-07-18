package osmConverter.constants;

import java.util.logging.Level;
import java.util.logging.Logger;

import osmConverter.data.OsmWay;
import osmConverter.io.IsLogging;

/**
 * Class providing functionality to get default values for lane-attribute.
 * 
 * @author Tom Müller
 * 
 */
public class LaneDefaults implements IsLogging {

    private Logger logger;
    private Logger osmLogger;

    boolean defaultValues = false;

    /**
     * 
     * Creates new Instance of class.
     * 
     * @param logName
     *            Logname to log conflicts.
     */
    public LaneDefaults(String[] logs) {
	logger = Logger.getLogger(logs[0]);
	osmLogger = Logger.getLogger(logs[2]);
    }

    /**
     * 
     * Creates new Instance with possibility to use streettype-dependend default
     * values.
     * 
     * @param logName
     * @param defaultValues
     */
    public LaneDefaults(String[] logs, boolean defaultValues) {
	logger = Logger.getLogger(logs[0]);
	osmLogger = Logger.getLogger(logs[2]);
	this.defaultValues = defaultValues;
    }

    /**
     * This returns 1 for every {@link OsmWay} which has no specific number of
     * lanes tagged. Except defaultValue is set true.
     * 
     * @param osmWay
     *            The {@link OsmWay} which's number of lanes is to get.
     * @return always 1 !!!
     */
    public int getNumberOfLanes(OsmWay osmWay) {

	int lanesDefault = 1;
	int lanes = 1;

	String highway = osmWay.getTags().get("highway");
	if (highway.equals("motorway")) {
	    lanesDefault = 2;
	} else if (highway.equals("trunk")) {
	    lanesDefault = 2;
	} else if (highway.equals("primary")) {
	    lanesDefault = 2;
	}

	if (lanes != lanesDefault) {
	    logOsmWarning("Conflict in lanes-tag (way-id: " + osmWay.getId()
		    + "! Highway = " + osmWay.getTags().get("highway")
		    + "). Default: " + lanesDefault + " is not 1!");
	}

	if (defaultValues) {
	    return lanesDefault;
	} else {
	    return lanes;
	}
    }

    private void logOsmWarning(String message) {
	osmLogger.log(Level.SEVERE, message);
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

}
