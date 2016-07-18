package osmConverter.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import osmConverter.io.IsLogging;

public class Comparer implements IsLogging {

    private Integer[] oldHighwayCount = new Integer[8];

    Logger osmLogger;

    public Integer[] getOldHighwayCount() {
	return oldHighwayCount;
    }

    public Comparer(String oldLogFile) {
	osmLogger = Logger.getLogger("output/comparison.log");

	readOldLog(oldLogFile);
    }

    private void readOldLog(String filename) {

	try {
	    FileReader reader = new FileReader(new File(filename));
	    BufferedReader breader = new BufferedReader(reader);

	    String line = "";
	    while ((line = breader.readLine()) != null) {
		if (line.contains("highwaycount")) {

		    String[] temp = line.split("\t");

		    this.oldHighwayCount[7] = Integer.parseInt(temp[2]
			    .split("=")[1]);

		    this.oldHighwayCount[0] = Integer.parseInt(temp[3]
			    .split("=")[1]);
		    this.oldHighwayCount[1] = Integer.parseInt(temp[4]
			    .split("=")[1]);
		    this.oldHighwayCount[2] = Integer.parseInt(temp[5]
			    .split("=")[1]);
		    this.oldHighwayCount[3] = Integer.parseInt(temp[6]
			    .split("=")[1]);
		    this.oldHighwayCount[4] = Integer.parseInt(temp[7]
			    .split("=")[1]);
		    this.oldHighwayCount[5] = Integer.parseInt(temp[8]
			    .split("=")[1]);
		    this.oldHighwayCount[6] = Integer.parseInt(temp[9]
			    .split("=")[1]);

		}
	    }

	    int i = 0;
	    i++;

	} catch (FileNotFoundException e) {
	    logError(e.getMessage());
	} catch (IOException e) {
	    logError(e.getMessage());
	}

    }

    public void compareLinkCount(Integer[] newLinkCount) {
	for (int i = 0; i < oldHighwayCount.length; i++) {

	    String temp = "";
	    double percent = (((double) newLinkCount[i] - (double) oldHighwayCount[i]) / newLinkCount[i]) * 100;

	    if (i == 7) {
		temp = "other categories";
	    } else {
		temp = "category " + i;
	    }

	    if (newLinkCount[i] >= oldHighwayCount[i]) {
		logInfo((newLinkCount[i] - oldHighwayCount[i])
			+ " new links of " + temp + ", this are " + percent
			+ "% of all links in this category!");
	    } else {
		logInfo((oldHighwayCount[i] - newLinkCount[i])
			+ " links less of " + temp + ", this are " + percent
			+ "% of all links in this category!");
	    }

	}
    }

    @Override
    public void logInfo(String message) {
	osmLogger.log(Level.INFO, message);
    }

    @Override
    public void logWarning(String message) {
	osmLogger.log(Level.WARNING, message);
    }

    @Override
    public void logError(String message) {
	osmLogger.log(Level.SEVERE, message);
    }

}
