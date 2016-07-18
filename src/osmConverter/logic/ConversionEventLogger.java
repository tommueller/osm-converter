package osmConverter.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import osmConverter.io.IsLogging;

public class ConversionEventLogger implements IsLogging {
    private Logger logger;

    private final List<String> highways = new ArrayList<String>();
    private final List<String> unreadHighways = new ArrayList<String>();
    private final Scorer scorer;

    public ConversionEventLogger(String loggerName) {
	logger = Logger.getLogger(loggerName);
	scorer = new Scorer(loggerName);
    }

    public List<String> getHighways() {
	return highways;
    }

    public List<String> getUnreadHighways() {
	return unreadHighways;
    }

    public Scorer getScorer() {
	return scorer;
    }

    public void reportUnknownHighway(String highway) {
	if (!unreadHighways.contains(highway)) {
	    logWarning("Converted unknown streettype: " + highway);
	    unreadHighways.add(highway);
	}
    }

    @Override
    public void logInfo(String message) {
	logger.log(Level.INFO, message);
    }

    @Override
    public void logWarning(String message) {
	logger.log(Level.WARNING, message);
    }

    @Override
    public void logError(String message) {
	logger.log(Level.SEVERE, message);
    }
}
