/**
 * 
 */
package osmConverter.io;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import osmConverter.data.OsmNode;
import osmConverter.data.OsmRelation;
import osmConverter.data.OsmWay;

/**
 * This reads an .osm-File and gets information into the program.
 * @author Tom Müller
 * @version 25.09.2010
 */
public class OsmReader extends DefaultHandler implements IsLogging {

    Logger logger;

    private final static String WAY = "way";
    private final static String NODE = "node";
    private final static String REL = "relation";

    private String currentTag = "";

    private final Map<String, OsmNode> nodes;
    private final Map<String, OsmWay> ways;
    private final Map<String, OsmRelation> relations;

    private OsmNode lastNode = null;
    private OsmWay lastWay = null;
    private OsmRelation lastRelation = null;

    public OsmReader(final Map<String, OsmNode> nodes,
	    final Map<String, OsmWay> ways,
	    Map<String, OsmRelation> osmRelations) throws Exception,
	    SAXException {
	logger = Logger.getLogger("defaultLog");

	this.nodes = nodes;
	this.ways = ways;
	this.relations = osmRelations;
    }

    private int counter = 0;
    private int secCounter = 0;

    @Override
    public void startDocument() throws SAXException {
	logInfo("Started parsing .osm-File!");
    }

    @Override
    public void endDocument() throws SAXException {
	logInfo("Ended parsing .osm-File!");
	counter = 0;
	secCounter = 0;
    }

    @Override
    public void startElement(String namespaceURI, String localName,
	    String type, Attributes atts) {

	logOutput();

	if ("node".equals(type)) {
	    String id = atts.getValue("id");
	    double lat = Double.parseDouble(atts.getValue("lat"));
	    double lon = Double.parseDouble(atts.getValue("lon"));

	    lastNode = new OsmNode(id, lon, lat);
	    currentTag = NODE;
	} else if ("way".equals(type)) {
	    this.lastWay = new OsmWay(atts.getValue("id"));

	    currentTag = WAY;
	} else if ("nd".equals(type)) {
	    if (this.lastWay != null) {
		this.lastWay.addNode(new OsmNode(atts.getValue("ref")));
	    }
	} else if ("tag".equals(type)) {
	    if (currentTag.equals(WAY)) {
		if (this.lastWay != null) {
		    this.lastWay.addTag(atts.getValue("k"), atts.getValue("v"));
		}
	    } else if (currentTag.equals(NODE)) {
		if (this.lastNode != null) {
		    this.lastNode
			    .addTag(atts.getValue("k"), atts.getValue("v"));
		}
	    } else if (currentTag.equals(REL)) {
		if (this.lastRelation != null) {
		    this.lastRelation.addTag(atts.getValue("k"),
			    atts.getValue("v"));
		}
	    }
	} else if ("relation".equals(type)) {
	    String id = atts.getValue("id");
	    lastRelation = new OsmRelation(id);

	    currentTag = REL;
	} else if ("member".equals(type)) {
	    if (atts.getValue("type").equals(WAY)) {
		
		OsmWay newWay = new OsmWay(atts.getValue("ref"));
		
		if (atts.getValue("role") != null) {
		    newWay.setRole(atts.getValue("role"));
		}
		
		lastRelation.addMember(newWay);
	    } else if (atts.getValue("type").equals(NODE)) {
		
		OsmNode newNode = new OsmNode(atts.getValue("ref"));
		
		if (atts.getValue("role") != null) {
		    newNode.setRole(atts.getValue("role"));
		}
		
		lastRelation.addMember(newNode);
	    } else if (atts.getValue("type").equals(REL)) {
		
		OsmRelation newRel = new OsmRelation(atts.getValue("ref"));
		
		if (atts.getValue("role") != null) {
		    newRel.setRole(atts.getValue("role"));
		}
		
		lastRelation.addMember(newRel);
	    }
	}

    }

    private void logOutput() {
	if (counter % (Math.pow(2., secCounter)) == 0) {
	    secCounter++;
	    logInfo("Read: " + counter + " tags.");
	}

	counter++;
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
	if (WAY.equals(qName)) {
	    this.ways.put(lastWay.getId(), lastWay);
	    this.lastWay = null;
	} else if (NODE.equals(qName)) {
	    this.nodes.put(lastNode.getId(), lastNode);
	    lastNode = null;
	} else if (REL.equals(qName)) {
	    this.relations.put(lastRelation.getId(), lastRelation);
	    lastNode = null;
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

    public void parseFile(String filename) throws ParserConfigurationException, SAXException, IOException {
	// read the file
	SAXParserFactory factory = SAXParserFactory.newInstance();
	SAXParser saxParser = factory.newSAXParser();
	saxParser.parse(new File(filename), this);
    }

}
