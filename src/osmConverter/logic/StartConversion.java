package osmConverter.logic;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.SAXException;

import osmConverter.data.StreetMap;
import osmConverter.io.IsLogging;
import osmConverter.io.MyFormatter;
import osmConverter.io.ShapeWriter;
import osmConverter.io.TomFileWriter;

/**
 * @author Tom Müller
 * @version 25.07.2010
 */
public class StartConversion implements IsLogging {

    private Logger logger;
    private Logger osmLogger;
    private Logger conversionLogger;

    private String file;

    boolean stats = false;
    boolean shape = false;
    boolean tom = false;
    boolean simpleMap = false;

    /**
     * @param args
     * @throws Exception
     * @throws SAXException
     */
    public static void main(String[] args) throws SAXException, Exception {
	StartConversion program = new StartConversion(args);
	program.run(args);
    }

    /**
     * Creates a new runnable instance of this program. Also reads the
     * command-line-arguments and decides which functions are requested.
     * 
     * @param args
     * @throws SecurityException
     * @throws IOException
     */
    public StartConversion(String[] args) throws SecurityException, IOException {
	super();

	boolean fileSet = false;

	for (int i = 0; i < args.length; i++) {

	    if (args[i].equals("-file")) {
		if (i >= args.length) {
		    System.out
			    .println("Fehlerhafte Paramter. Auf -file muss eine gültige Dateiangabe folgen!");
		} else {
		    this.file = args[i + 1];
		    fileSet = true;
		}
	    } else if (args[i].equals("-writeShapes")) {
		shape = true;
	    } else if (args[i].equals("-simpleMap")) {
		simpleMap = true;
	    } else if (args[i].equals("-writeTomFiles")) {
		tom = true;
	    } else if (args[i].equals("-writeStats")) {
		stats = true;
	    } else if (args[i].endsWith(".osm")) {
	    } else {
		System.out.println("Unbekannter Parameter: " + args[i] + " !");
	    }

	}

	if (!fileSet) {
	    System.out
		    .println("Keine zu konvertierende Datei angegeben! Programm bricht ab!");
	    return;
	}

	// try to create output-folders if they don't exist.

	try {
	    String[] dirNames = { "output", "output/shape", "output/charts",
		    "output/stats" };

	    for (String dirName : dirNames) {

		File f = new File(dirName);
		if (f.isDirectory()) {
		} else {
		    f.mkdir();
		}
	    }
	} catch (Exception e) {
	    System.out
		    .println("Konnte Ausgabeordner nicht erstellen. Programm bricht ab!");
	    return;
	}

	logger = initLog(logger, "defaultLog");
	osmLogger = initLog(osmLogger, "osmLog");
	conversionLogger = initLog(conversionLogger, "conversionLog");
    }

    private void run(String[] args) throws SAXException, Exception {

	// create logs

	String logs[] = { "defaultLog", "conversionLog", "osmLog" };

	Converter mapConverter = new Converter(logs);
	MapAnalyzer mapAnalyzer = new MapAnalyzer(logs);

	// convert map
	StreetMap streetMap = mapConverter.convertMap(file, simpleMap);

	// analyze category-, lane-, name- and speed-tags.

	if (stats) {
	    mapAnalyzer.analyzeCategorySizes(streetMap, file);
	    mapAnalyzer.analyzeLaneTag(mapConverter.getConversionLogger()
		    .getScorer().getLanes(), mapConverter.getConversionLogger()
		    .getScorer().getNoLanes(), file);
	    mapAnalyzer.analyzeNameTag(mapConverter.getConversionLogger()
		    .getScorer().getNamedHighways(), mapConverter
		    .getConversionLogger().getScorer().getUnnamedHighways(),
		    file);
	    mapAnalyzer.analyzeSpeedTag(mapConverter.getConversionLogger()
		    .getScorer().getHighwaySpeeds(), file);
	}

	// output map and simple map in different forms

	ShapeWriter sw = new ShapeWriter();
	TomFileWriter tfw = new TomFileWriter();

	if (shape) {
	    sw.writeStreetShape(streetMap.getStreets(), "output/shape/streets");
	    sw.writeLinkShape(streetMap.getLinks(), "output/shape/links");
	}

	if (tom) {
	    tfw.writeTomFile("edgeCoords.tom", streetMap, "\t", true);
	}
    }

    private Logger initLog(Logger logger, String logName)
	    throws SecurityException, IOException {
	logger = Logger.getLogger(logName);
	FileHandler fh;

	// This block configures the logger with handler and formatter
	fh = new FileHandler("output/" + logName + ".log", false);
	logger.addHandler(fh);
	logger.setLevel(Level.ALL);
	MyFormatter formatter = new MyFormatter();
	fh.setFormatter(formatter);

	if (logName.equals("osmLog") || logName.equals("conversionLog")) {
	    ConsoleHandler ch = new ConsoleHandler();
	    ch.setLevel(Level.OFF);
	    logger.addHandler(ch);
	    logger.setUseParentHandlers(false);
	}

	return logger;
    }

    @Override
    public void logInfo(String message) {
	conversionLogger.log(Level.INFO, message);
    }

    @Override
    public void logWarning(String message) {
	conversionLogger.log(Level.WARNING, message);
    }

    @Override
    public void logError(String message) {
	conversionLogger.log(Level.SEVERE, message);
    }

}
