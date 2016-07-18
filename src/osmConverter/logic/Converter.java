package osmConverter.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.SAXException;

import osmConverter.constants.HighwayDefaults;
import osmConverter.constants.LaneDefaults;
import osmConverter.constants.SpeedDefaults;
import osmConverter.data.Link;
import osmConverter.data.MyArea;
import osmConverter.data.Node;
import osmConverter.data.OsmNode;
import osmConverter.data.OsmObject;
import osmConverter.data.OsmRelation;
import osmConverter.data.OsmWay;
import osmConverter.data.PointOfInterest;
import osmConverter.data.Restriction;
import osmConverter.data.Street;
import osmConverter.data.StreetMap;
import osmConverter.io.IsLogging;
import osmConverter.io.OsmReader;
import osmConverter.io.TomFileWriter;

/**
 * This class provides all functionality to convert an osm-map to a normed map.
 * There are different possibilities to output the converted map.
 * 
 * @author Tom Müller
 * @version 26.07.2010
 */
public class Converter implements IsLogging, IConverter {

    private ConversionEventLogger conversionLogger;
    int trafficZoneSet = 0;

    private boolean firstTimeUntyped = true;

    private HighwayDefaults highwayDefaults = new HighwayDefaults();

    private final LaneDefaults laneDefaults;
    private Map<String, Link> links = new HashMap<String, Link>();
    private Logger logger;

    private int maxspeedTotal = 0;

    private final Map<String, MyArea> myAreas = new HashMap<String, MyArea>();
    private Map<String, Node> nodes = new HashMap<String, Node>();
    private Logger osmLogger;
    private final Map<String, OsmNode> osmNodes = new HashMap<String, OsmNode>();
    private final Map<String, OsmRelation> osmRelations = new HashMap<String, OsmRelation>();

    private final Map<String, OsmWay> osmWays = new HashMap<String, OsmWay>();
    private List<PointOfInterest> pointOfInterest = new ArrayList<PointOfInterest>();

    private int restCount = 0;

    private List<Restriction> restrictions = new ArrayList<Restriction>();

    private SpeedDefaults speedDefaults;
    private int speedSet = 0;

    private StreetMap streetMap;

    private Map<String, Node> tempNodes = new HashMap<String, Node>();
    
    private final String[] logs;

    /**
     * Creates a new {@link Converter} which converts an osm-map to a specified
     * output format.
     * 
     * @param logs
     *            The name of the default logger.
     */
    public Converter(String[] logs) {
	super();
	logger = Logger.getLogger(logs[0]);
	conversionLogger = new ConversionEventLogger(logs[1]);
	osmLogger = Logger.getLogger(logs[2]);
	
	laneDefaults = new LaneDefaults(logs);
	
	this.logs = logs;
    }

    private void assignCoordsAndTagsToLinkNodes() {
	for (OsmWay osmWay : osmWays.values()) {
	    for (OsmNode node : osmWay.getNodes()) {
		node.setLon(osmNodes.get(node.getId()).getLon());
		node.setLat(osmNodes.get(node.getId()).getLat());
		node.setTags(osmNodes.get(node.getId()).getTags());
	    }
	}
    }

    private void checkForTagConflicts(OsmWay osmWay, Link link) {

	// check for conflicts concerning maxspeed tag
	speedDefaults.checkForSpeedConflicts(osmWay, link);
    }

    private void checkIfAllReferencedNodesExist() {
	int counter = 0;

	for (Link link : links.values()) {
	    if (!tempNodes.containsKey(link.getStartNodeId())
		    || !tempNodes.containsKey(link.getEndNodeId())) {
		logError("Link : " + link.getId().toString()
			+ " is missing a node!");
		counter++;
	    }
	}

	if (counter != 0) {
	    logWarning(counter + " missing Nodes");
	}
    }

    /**
     * Remove restrictions which are not constructible
     * 
     * @param ids
     */
    private void cleanProhibitedManeuvers(Map<String, Collection<Link>> ids) {
	for (Iterator<Restriction> it = restrictions.iterator(); it.hasNext();) {

	    Restriction rest = it.next();

	    if (!ids.containsKey(rest.getFrom())
		    || !ids.containsKey(rest.getTo())) {
		it.remove();
	    }
	}
    }

    /**
     * Converts an {@link OsmWay} into several {@link Link}s. The attributes are
     * first set for the whole way and then just copied to every new sub-link.
     * The splitting is done to avoid having "between-nodes" which are quite
     * uncomfortable for some tasks. Also this method creates reverted copies of
     * all links which are not oneway! This is essential for routing algorithms
     * like Dijkstra!
     * 
     * @param osmWay
     *            The {@link OsmWay} which shall be converted.
     * @param firstId
     *            Index for the first Id (necessary for a unique Id)
     * @return {@link Map} of the newly generated {link Link}s.
     */
    public Map<String, Link> convertHighway(OsmWay osmWay, int firstId) {

	if (osmWay.getTags().containsKey("highway")) {

	    Link link = new Link();

	    // set the street-type
	    link.setStreetType(getStreettype(osmWay));

	    // set the street-category
	    link.setStreetCategory(getStreetcategory(osmWay));

	    // set the name
	    link.setName(getStreetName(osmWay));

	    // set the max. speed
	    link.setSpeed(getMaxspeed(osmWay, link));

	    // set the restriction id
	    link.setRestrictions(getRestricitions(osmWay));

	    // set oneway-tag. as all streets are oneway (no bidirectional
	    // links!) this tag is true if there is no link which describes the
	    // opposite direction of the original link

	    link.setOneWay(getOneWay(osmWay));

	    // set the number of lanes

	    link.setNumberOfLanes(getNumberOfLanes(osmWay, link));

	    // check if there are any conflicts between different tags of the
	    // way
	    checkForTagConflicts(osmWay, link);

	    // split the links and make them one-ways
	    return splitLinks(osmWay, firstId, link);
	} else {
	    return null;
	}

    }

    /**
     * 
     * convert all way entities with highway tag.
     * 
     * @param idAssignment
     */
    private void convertHighways(Map<String, Collection<Link>> idAssignment) {
	List<String> onlyPrintItOnce = new ArrayList<String>();
	for (OsmWay osmWay : osmWays.values()) {
	    if (osmWay.getTags().containsKey("highway")) {

		// skip the conversion of ways which are irrelevant for the map

		if (osmWay.getTags().get("highway").equals("")
			|| osmWay.getTags().get("highway").equals("proposed")
			|| osmWay.getTags().get("highway").equals("dismantled")
			|| osmWay.getTags().get("highway")
				.equals("construction")
			|| osmWay.getTags().get("highway").equals("raceway")
			|| osmWay.getTags().get("highway").equals("planned")) {

		    if (!onlyPrintItOnce.contains(osmWay.getTags().get(
			    "highway"))) {
			logInfo("Ways of type highway = "
				+ osmWay.getTags().get("highway") + " skipped!");
			onlyPrintItOnce.add(osmWay.getTags().get("highway"));
		    }
		    continue;
		}

		Map<String, Link> currentLinks = this.convertHighway(osmWay,
			links.size());
		if (currentLinks != null) {
		    links.putAll(currentLinks);
		    idAssignment.put(osmWay.getId(), currentLinks.values());
		}
	    }
	}
    }

    /**
     * Converts a complete OSM-Map without producing a converted map-file.
     * 
     * @param filename
     *            The .osm-file to be converted.
     * @param b
     * @return The converted street-map.
     * @throws SAXException
     * @throws Exception
     */
    public StreetMap convertMap(String filename, boolean b)
	    throws SAXException, Exception {
	// init the reader
	OsmReader osmReader = new OsmReader(osmNodes, osmWays, osmRelations);
	osmReader.parseFile(filename);

	// start to convert the map
	logInfo("starting conversion");

	speedDefaults = new SpeedDefaults(osmNodes, logs,
		conversionLogger);

	// give OsmWay.nodes their coordinates

	assignCoordsAndTagsToLinkNodes();
	convertNodes();

	for (OsmWay osmWay : osmWays.values()) {
	    if (osmWay.getTags().containsKey("area")) {
		this.createUrbanAreas(osmWay);
	    }
	}

	// convert relations

	for (OsmRelation osmRelation : osmRelations.values()) {
	    if (osmRelation.getTags().containsKey("area")
		    || osmRelation.getTags().containsKey("boundary")) {
		this.createUrbanAreas(osmRelation);
	    } else if (osmRelation.getTags().containsKey("type")) {
		if (osmRelation.getTags().get("type").equals("restriction")) {
		    Restriction rest = new Restriction();
		    if (rest.createRestriction(osmRelation)) {
			restrictions.add(rest);
		    }
		}
	    }
	}

	Map<String, Collection<Link>> idAssignment = new HashMap<String, Collection<Link>>();
	convertHighways(idAssignment);

	conversionLogger.logInfo("new map contains links: " + links.size());
	conversionLogger.logInfo("new map contains nodes: " + tempNodes.size());
	conversionLogger.logInfo("number of restrictions on relevant links: "
		+ restCount);
	conversionLogger.logInfo("number of deleted maneuvers: "
		+ restrictions.size());

	conversionLogger.logInfo("number of traffic:zone-tags: "
		+ trafficZoneSet);

	checkIfAllReferencedNodesExist();
	removeUnusedNodes();

	this.deriveEdgeData();

	this.deleteProhibitedManeuvers(idAssignment);

	createStreetMap(b);

	return streetMap;
    }

    /**
     * Converts a complete OSM-Map and writes it to the specified output-file.
     * 
     * @param filename
     *            The .osm-file to be converted.
     * @param outFile
     *            The filename to write the converted map to.
     * @return The converted street-map.
     * @throws SAXException
     * @throws Exception
     */
    public StreetMap convertMap(String osmFile, String outFile)
	    throws SAXException, Exception {
	StreetMap map = this.convertMap(osmFile, false);
	TomFileWriter writer = new TomFileWriter();

	writer.writeTomFile(outFile, streetMap, "\t", false);
	return map;
    }

    /**
     * Converts a single {@link OsmNode} to a Node-object.
     * 
     * @param osmNode
     *            The {@link OsmNode} to be converted.
     * @return The newly created {@link Node}
     */
    public Node convertNode(OsmNode osmNode) {
	Node node;

	// try to decide whether it is just a node or has some special abilities
	// :)
	if (!(osmNode.getTags().size() > 1)
		|| (osmNode.getTags().size() == 1 && osmNode.getTags()
			.containsKey("created_by"))) {
	    node = new Node();
	} else {
	    node = createPOI(osmNode);
	}

	// set the relevant data
	node.setId(osmNode.getId());
	node.setLon(osmNode.getLon());
	node.setLat(osmNode.getLat());

	return node;
    }

    /**
     * Convert nodes and decide whether they are point of interests or not.
     */
    private void convertNodes() {
	for (OsmNode osmNode : osmNodes.values()) {
	    Node node = this.convertNode(osmNode);
	    if (!(node instanceof PointOfInterest)) {
		tempNodes.put(node.getId(), node);
	    } else {
		if (osmNode.isRealPOI()) {
		    PointOfInterest poi = (PointOfInterest) node;
		    poi.deriveType();
		    if (poi.getType() != null) {
			pointOfInterest.add(poi);
		    }
		}
		tempNodes.put(node.getId(), node);
	    }
	}
    }

    /**
     * Create an advanced node, which also inherits the tags from its original
     * {@link OsmWay}.
     * 
     * @param osmNode
     * @return
     */
    private Node createPOI(OsmNode osmNode) {
	PointOfInterest node = new PointOfInterest();
	node.setTags(osmNode.getTags());

	return node;
    }

    /**
     * Create the StreetMap!
     * 
     * @param b
     *            Is true if the simple-map shall be generated.
     */
    private void createStreetMap(boolean b) {
	streetMap = new StreetMap();

	streetMap.setLinks(links);
	streetMap.setNodes(tempNodes);

	if (b)
	    streetMap.setStreets(this.generateSimpleMap());
    }

    /**
     * Create an area-object. Actually useless since both area-types cant be
     * used to decide whether a point is inside or outside of a city.
     * 
     * @param osmObject
     *            The osmObject to be converted.
     */
    private void createUrbanAreas(OsmObject osmObject) {

	MyArea myArea = new MyArea();

	// set area-type

	if (osmObject.getTags().containsKey("place")) {
	    if (osmObject.getTags().get("place").equals("city")
		    || osmObject.getTags().get("place").equals("town")
		    || osmObject.getTags().get("place").equals("village")) {
		myArea.setType("town");
	    } else if (osmObject.getTags().get("place").equals("suburb")) {
		myArea.setType("suburb");
	    } else {
		return;
	    }
	} else if (osmObject.getTags().containsKey("boundary")
		&& osmObject.getTags().containsKey("admin_level")) {
	    if (osmObject.getTags().get("boundary").equals("administrative")
		    && osmObject.getTags().get("admin_level").equals("4")) {
		myArea.setType("town");
	    } else if (osmObject.getTags().get("boundary").equals("town")
		    && osmObject.getTags().get("admin_level").equals("8")) {
		myArea.setType("town");
	    } else if (osmObject.getTags().get("boundary")
		    .equals("administrative")) {
		int temp = Integer.parseInt(osmObject.getTags().get(
			"admin_level"));
		if (temp == 9 || temp == 10) {
		    myArea.setType("suburb");
		}
	    } else {
		return;
	    }
	} else {
	    return;
	}

	// create area
	if (osmObject instanceof OsmWay) {
	    myArea.createAreaFromWay((OsmWay) osmObject);
	} else if (osmObject instanceof OsmRelation) {
	    myArea.createAreaFromRelation((OsmRelation) osmObject, osmWays,
		    osmNodes);
	} else {
	    return;
	}

	myArea.addTags(osmObject.getTags());
	myArea.setId(osmObject.getId());

	if (myArea.getLatCoordinates() == null) {
	    return;
	}

	this.myAreas.put(myArea.getId(), myArea);
    }

    /**
     * Removes all prohibited-maneuvers from following- and leading-edges.
     * 
     * @param ids
     */
    private void deleteProhibitedManeuvers(Map<String, Collection<Link>> ids) {

	List<Restriction> temp = new ArrayList<Restriction>();

	// remove all prohibitions which ways belong to unconverted ways (like
	// paths etc)

	this.cleanProhibitedManeuvers(ids);

	// delete the restricted maneuvers

	for (Restriction rest : restrictions) {

	    boolean removed = false;

	    // remove restriction prohibiting a specific maneuver.
	    if (rest.getType().equals("no")) {

		for (Link from : ids.get(rest.getFrom())) {
		    for (Link to : ids.get(rest.getTo())) {
			if (links.get(from.getId()).getFollowingLinks()
				.contains(to.getId())) {
			    links.get(from.getId()).getFollowingLinks()
				    .remove(to.getId());
			    links.get(to.getId()).getLeadingLinks()
				    .remove(from.getId());

			    removed = true;
			}
		    }
		}

		// remove restriction just allowing to drive into one direction
	    } else if (rest.getType().equals("only")) {

		for (Link from : ids.get(rest.getFrom())) {
		    for (Link to : ids.get(rest.getTo())) {
			if (links.get(from.getId()).getFollowingLinks()
				.contains(to.getId())) {

			    List<String> follow = new ArrayList<String>();
			    follow.add(to.getId());
			    links.get(from.getId()).setFollowingLinks(follow);

			    List<String> lead = new ArrayList<String>();
			    lead.add(from.getId());
			    links.get(to.getId()).setLeadingLinks(lead);

			    removed = true;
			}
		    }
		}
	    }

	    // log unconstructible
	    if (!removed) {
		logOsmWarning("Restriction " + rest.getId()
			+ " couldn't be contructed");
		temp.add(rest);
	    }
	}

	for (Restriction rest : temp) {
	    restrictions.remove(rest);
	}
    }

    /**
     * Gets the following and leading links for every link.
     * 
     * @param streetMap
     */
    private void deriveEdgeData() {
	Map<String, ArrayList<String>> leader = new HashMap<String, ArrayList<String>>();
	Map<String, ArrayList<String>> follower = new HashMap<String, ArrayList<String>>();

	for (Link link : links.values()) {

	    if (leader.containsKey(link.getEndNodeId())) {
		ArrayList<String> temp = leader.get(link.getEndNodeId());
		temp.add(link.getId());
		leader.put(link.getEndNodeId(), temp);
	    } else {
		ArrayList<String> temp = new ArrayList<String>();
		temp.add(link.getId());
		leader.put(link.getEndNodeId(), temp);
	    }

	    if (follower.containsKey(link.getStartNodeId())) {
		ArrayList<String> temp = follower.get(link.getStartNodeId());
		temp.add(link.getId());
		follower.put(link.getStartNodeId(), temp);
	    } else {
		ArrayList<String> temp = new ArrayList<String>();
		temp.add(link.getId());
		follower.put(link.getStartNodeId(), temp);
	    }
	}

	for (Link link : links.values()) {

	    ArrayList<String> tempFollow = follower.get(link.getEndNodeId());
	    if (tempFollow == null) {
		tempFollow = new ArrayList<String>();
	    }

	    ArrayList<String> follow = new ArrayList<String>();

	    for (String id : tempFollow) {
		if ((links.get(id).getStartNodeId()
			.equals(link.getStartNodeId()) && links.get(id)
			.getEndNodeId().equals(link.getEndNodeId()))
			|| links.get(id).getStartNodeId()
				.equals(link.getEndNodeId())
			&& links.get(id).getEndNodeId()
				.equals(link.getStartNodeId())) {
		} else {
		    follow.add(id);
		}
	    }

	    ArrayList<String> tempLead = leader.get(link.getStartNodeId());
	    ArrayList<String> lead = new ArrayList<String>();

	    if (tempLead == null) {
		tempLead = new ArrayList<String>();
	    }

	    for (String id : tempLead) {
		if ((links.get(id).getStartNodeId()
			.equals(link.getStartNodeId()) && links.get(id)
			.getEndNodeId().equals(link.getEndNodeId()))
			|| links.get(id).getStartNodeId()
				.equals(link.getEndNodeId())
			&& links.get(id).getEndNodeId()
				.equals(link.getStartNodeId())) {
		} else {
		    lead.add(id);
		}
	    }

	    link.setFollowingLinks(follow);
	    link.setLeadingLinks(lead);
	}
    }

    /**
     * Generate the minimum-map for better software-performance.
     * 
     * @return the minimum-map
     */
    private Map<Integer, Street> generateSimpleMap() {

	Map<Integer, Street> streets = new HashMap<Integer, Street>();
	List<Integer> help = new ArrayList<Integer>();
	int id = 0;

	for (Link link : links.values()) {
	    if (link.getStreetType() < 5
		    && !help.contains(Integer.parseInt(link.getId()))) {
		List<String> edgeIDs = new ArrayList<String>();
		edgeIDs.add(link.getId());
		double length = link.getLength();
		Street coords = new Street();
		coords.setId(id);
		coords.setStartLon(link.getStartLon());
		coords.setStartLat(link.getStartLat());
		coords.setLeadingStreets(link.getLeadingLinks());
		coords.setEndLon(link.getEndLon());
		coords.setEndLat(link.getEndLat());
		coords.setFollowingStreets(link.getFollowingLinks());
		coords.setStreetType(link.getStreetType());
		coords.setName(link.getName());
		coords.setSpeed(link.getSpeed());
		links.get(link.getId()).setStreetID(id);
		help.add(Integer.parseInt(link.getId()));
		Link acLink = links.get(link.getId());
		boolean found = true;
		while (found) {
		    // test for crossings
		    found = false;
		    int count = 0;
		    String fid = "";
		    for (String follow : acLink.getFollowingLinks()) {
			if (links.get(follow).getStreetType() < 5) {
			    count++;
			    fid = follow;
			}
		    }

		    // if not a crossing, check if there's just one leading-link

		    if (count == 1) {
			found = true;
			Link followEdge = links.get(fid);
			count = 0;
			for (String follow : followEdge.getLeadingLinks()) {
			    if (links.get(follow).getStreetType() < 5) {
				count++;
			    }
			}

			// if also maxspeed and name are the same, the link
			// wasn't already treated and its not the opposite
			// direction: add it to the street.

			if (count != 1
				|| !followEdge.getName().equals(
					acLink.getName())
				|| followEdge.getSpeed() != acLink.getSpeed()
				|| help.contains(Integer.parseInt(followEdge
					.getId()))
				|| acLink.getStartNodeId().equals(
					followEdge.getEndNodeId())) {
			    found = false;
			}
			if (found) {
			    coords.setEndLon(followEdge.getEndLon());
			    coords.setEndLat(followEdge.getEndLat());
			    coords.setFollowingStreets(followEdge
				    .getFollowingLinks());
			    edgeIDs.add(followEdge.getId());
			    help.add(Integer.parseInt(followEdge.getId()));
			    length += followEdge.getLength();
			    links.get(followEdge.getId()).setStreetID(id);
			    acLink = followEdge;
			}
		    }
		}
		// opposite direction
		found = true;
		while (found) {
		    // test for crossings
		    found = false;
		    int count = 0;
		    String fid = "";
		    for (String follow : link.getLeadingLinks()) {
			if (links.get(follow).getStreetType() < 5) {
			    count++;
			    fid = follow;
			}
		    }

		    // if not a crossing, check if there's just one leading-link

		    if (count == 1) {
			found = true;
			Link followEdge = links.get(fid);
			count = 0;
			for (String follow : followEdge.getFollowingLinks()) {
			    if (links.get(follow).getStreetType() < 5) {
				count++;
			    }
			}

			// if also maxspeed and name are the same, the link
			// wasn't already treated and its not the opposite
			// direction: add it to the street.

			if (count != 1
				|| !followEdge.getName().equals(link.getName())
				|| followEdge.getSpeed() != link.getSpeed()
				|| help.contains(Integer.parseInt(followEdge
					.getId()))
				|| link.getEndNodeId().equals(
					followEdge.getStartNodeId())) {
			    found = false;
			}
			if (found) {
			    coords.setStartLon(followEdge.getStartLon());
			    coords.setStartLat(followEdge.getStartLat());
			    coords.setLeadingStreets(followEdge
				    .getLeadingLinks());
			    edgeIDs.add(0, followEdge.getId());
			    help.add(Integer.parseInt(followEdge.getId()));
			    length += followEdge.getLength();
			    links.get(followEdge.getId()).setStreetID(id);
			    link = followEdge;
			}
		    }
		}
		coords.setLength(length);
		coords.setEdgeIDs(edgeIDs);
		streets.put(id, coords);
		id++;
	    }
	}
	for (Street street : streets.values()) {
	    ArrayList<String> sIDs = new ArrayList<String>();
	    ArrayList<String> lIDs = new ArrayList<String>();
	    for (String ids : street.getFollowingStreets()) {
		if (!sIDs.contains(links.get(ids).getStreetID())) {
		    sIDs.add(String.valueOf(links.get(ids).getStreetID()));
		}
	    }
	    streets.get(street.getId()).setFollowingStreets(sIDs);
	    for (String ids : street.getLeadingStreets()) {
		if (!lIDs.contains(links.get(ids).getStreetID())) {
		    lIDs.add(String.valueOf(links.get(ids).getStreetID()));
		}
	    }
	    streets.get(street.getId()).setLeadingStreets(lIDs);
	}

	double relLinks = 0;

	for (Link link : links.values()) {
	    if (link.getStreetType() < 5) {
		relLinks++;
	    }
	}

	conversionLogger.logInfo("relevant links before: " + relLinks
		+ "\tLinks in simple-map: " + id + "\t"
		+ (double) (relLinks - id) / relLinks + "% reduction!");

	return streets;
    }

    /**
     * 
     * @return The ConversionEventLogger.
     */
    public ConversionEventLogger getConversionLogger() {
	return conversionLogger;
    }

    /**
     * Gets the maximum speed for the {@link OsmWay}. Tries to read the value
     * from {@code maxspeed}-tag, if this isn't set the value is derived from
     * defaults.
     * 
     * @param osmWay
     *            {@link OsmWay} to get maximum speed for.
     * @return the maximum speed
     */
    private int getMaxspeed(OsmWay osmWay, Link link) {

	boolean zoneSet = false;
	boolean zoneUrban = false;

	if (osmWay.getTags().containsKey("zone:traffic")) {
	    zoneSet = true;
	    trafficZoneSet++;
	    if (osmWay.getTags().get("zone:traffic").equals("urban")) {
		zoneUrban = true;
	    }
	}

	this.maxspeedTotal++;

	// if the {@link OsmWay} has a maxspeed-tag read it
	if (osmWay.getTags().containsKey("maxspeed")) {

	    this.speedSet++;

	    String maxspeed = osmWay.getTags().get("maxspeed");
	    try {
		// this makes sure that there are no negativ values for maxspeed
		if (Integer.valueOf(maxspeed) > -1) {
		    conversionLogger.getScorer().reportExplicitMaxSpeed(osmWay);
		    return Integer.valueOf(maxspeed);
		} else {
		    logWarning("Not a valid number for maxspeed at way-id: "
			    + osmWay.getId());
		    if (zoneSet) {
			link.setDefaultless(false);
			return speedDefaults.getDefaultSpeed(
				link.getStreetType(), osmWay, myAreas,
				osmNodes, zoneUrban);
		    } else {
			link.setDefaultless(false);
			return speedDefaults
				.getDefaultSpeed(link.getStreetType(), osmWay,
					myAreas, osmNodes);
		    }
		}
	    } catch (NumberFormatException e) {

		// if the maxspeed-tag does not contain a number but a word try
		// to convert that value

		if (maxspeed.equals("walk")) {
		    /* This tag is deprecated, but not completely removed ... */
		    conversionLogger.getScorer().reportNANMaxSpeed(osmWay);
		    return SpeedDefaults.WALK_SPEED;
		} else if (maxspeed.equals("moderat")) {
		    /* Fahrradstraßen -> kleiner 30 */
		    conversionLogger.getScorer().reportNANMaxSpeed(osmWay);
		    return SpeedDefaults.MODERAT_SPEED;
		} else if (maxspeed.equals("DE:living_street")) {
		    /* Verkehrsberuhigter Bereich */
		    conversionLogger.getScorer().reportNANMaxSpeed(osmWay);
		    return SpeedDefaults.WALK_SPEED;
		} else if (maxspeed.equals("DE:rural")) {
		    /* Landstraße */
		    conversionLogger.getScorer().reportNANMaxSpeed(osmWay);
		    return SpeedDefaults.RURAL_SPEED;
		} else if (maxspeed.equals("DE:urban")) {
		    /* Landstraße */
		    conversionLogger.getScorer().reportNANMaxSpeed(osmWay);
		    return SpeedDefaults.URBAN_SPEED;
		} else if (maxspeed.equals("DE:motorway")) {
		    /* Autobahn */
		    conversionLogger.getScorer().reportNANMaxSpeed(osmWay);
		    return SpeedDefaults.MOTORWAY;
		} else if (maxspeed.equals("none")) {
		    /* Autobahn */
		    conversionLogger.getScorer().reportNANMaxSpeed(osmWay);
		    return SpeedDefaults.NO_MAXSPEED;
		} else if (maxspeed.equals("signals")) {
		    /*
		     * not clear what to do about this. getting default speed
		     * for the time being
		     */
		    link.setDefaultless(false);
		    conversionLogger.getScorer().reportDefaultMaxSpeed(osmWay);
		    if (zoneSet) {
			return speedDefaults.getDefaultSpeed(
				link.getStreetType(), osmWay, myAreas,
				osmNodes, zoneUrban);
		    } else {
			return speedDefaults
				.getDefaultSpeed(link.getStreetType(), osmWay,
					myAreas, osmNodes);
		    }
		} else {

		    // if the word is not known yet, print a warning and get
		    // default speed

		    link.setDefaultless(false);

		    logWarning("unexpected value for tag maxspeed: "
			    + e.getMessage() + "\tTrying to get default speed.");
		    conversionLogger.getScorer().reportDefaultMaxSpeed(osmWay);
		    if (zoneSet) {
			return speedDefaults.getDefaultSpeed(
				link.getStreetType(), osmWay, myAreas,
				osmNodes, zoneUrban);
		    } else {
			return speedDefaults
				.getDefaultSpeed(link.getStreetType(), osmWay,
					myAreas, osmNodes);
		    }
		}
	    } catch (Exception e) {

		// if the conversion fails but not because of a
		// NumberFormatException print a warning and get default speed.

		link.setDefaultless(false);

		logWarning("Error occurred: " + e.getMessage()
			+ "\tTrying to get default speed.");
		conversionLogger.getScorer().reportDefaultMaxSpeed(osmWay);
		if (zoneSet) {
		    return speedDefaults.getDefaultSpeed(link.getStreetType(),
			    osmWay, myAreas, osmNodes, zoneUrban);
		} else {
		    return speedDefaults.getDefaultSpeed(link.getStreetType(),
			    osmWay, myAreas, osmNodes);
		}
	    }
	} else {

	    // if the way has no maxspeed-tag get default speed

	    link.setDefaultless(false);

	    conversionLogger.getScorer().reportDefaultMaxSpeed(osmWay);
	    if (zoneSet) {
		return speedDefaults.getDefaultSpeed(link.getStreetType(),
			osmWay, myAreas, osmNodes, zoneUrban);
	    } else {
		return speedDefaults.getDefaultSpeed(link.getStreetType(),
			osmWay, myAreas, osmNodes);
	    }
	}
    }

    /**
     * Get number of lanes for the osmWay. Reads the tag if available. Sets
     * default if not.
     * 
     * @param osmWay
     *            converted osmWay.
     * @param link
     *            Target link.
     * @return number of Lanes.
     */
    private int getNumberOfLanes(OsmWay osmWay, Link link) {
	if (osmWay.getTags().containsKey("lanes")) {
	    try {
		int lanes = Integer.parseInt(osmWay.getTags().get("lanes"));

		conversionLogger.getScorer().reportExistingLaneTag(osmWay);

		if (this.getOneWay(osmWay)) {
		    return lanes;
		} else {

		    int reLanes = ((lanes / 2) > 1) ? (lanes / 2) : 1;

		    return reLanes;
		}
	    } catch (Exception e) {

		link.setDefaultless(false);

		conversionLogger.getScorer().reportWrongLaneTag(osmWay);
		return laneDefaults.getNumberOfLanes(osmWay);
	    }
	} else {

	    link.setDefaultless(false);

	    conversionLogger.getScorer().reportNotExistingLaneTag(osmWay);

	    return laneDefaults.getNumberOfLanes(osmWay);
	}
    }

    /**
     * Get if the osmWay is a oneway road.
     * 
     * @param osmWay
     * @return true if is oneway.
     */
    private boolean getOneWay(OsmWay osmWay) {
	boolean oneWay = osmWay.getTags().containsKey("oneway");
	if (oneWay) {
	    if (osmWay.getTags().get("oneway").equals("yes")
		    || osmWay.getTags().get("oneway").equals("1")
		    || osmWay.getTags().get("oneway").equals("true")) {
		oneWay = true;
	    } else if (osmWay.getTags().get("oneway").equals("-1")) {
		this.reverseWay(osmWay);
	    } else {
		oneWay = false;
	    }
	}

	return oneWay;
    }

    /**
     * Derives the restriction-id from {@link OsmWay}s tags.
     * 
     * @param osmWay
     *            {@link OsmWay} to derivate the restrictions for.
     * @return Id which describes the restrictions (see {@link Link})
     */
    private int getRestricitions(OsmWay osmWay) {
	Map<String, String> tags = osmWay.getTags();

	// get the restriction id from access-tag

	if (tags.containsKey("access")) {

	    restCount++;

	    String access = tags.get("access");
	    if (access.equals("yes")) {
		return 1;
	    } else if (access.equals("no")) {
		return 0;
	    } else if (access.equals("permissive")) {
		return 1;
	    } else if (access.equals("official")) {
		return 1;
	    } else if (access.equals("private")) {
		return 2;
	    } else if (access.equals("destination")) {
		return 2;
	    } else if (access.equals("agricultural")) {
		return 0;
	    } else if (access.equals("forestry")) {
		return 0;
	    } else if (access.equals("unknown")) {
		return 2;
	    } else if (access.equals("designated")) {
		return 1;
	    } else if (access.equals("delivery")) {
		return 3;
	    }
	}

	if (tags.containsKey("vehicle")) {
	    restCount++;

	    String access = tags.get("vehicle");
	    if (access.equals("yes")) {
		return 1;
	    } else if (access.equals("no")) {
		return 0;
	    } else if (access.equals("permissive")) {
		return 1;
	    } else if (access.equals("official")) {
		return 1;
	    } else if (access.equals("private")) {
		return 2;
	    } else if (access.equals("destination")) {
		return 2;
	    } else if (access.equals("agricultural")) {
		return 0;
	    } else if (access.equals("forestry")) {
		return 0;
	    } else if (access.equals("unknown")) {
		return 2;
	    } else if (access.equals("designated")) {
		return 1;
	    } else if (access.equals("delivery")) {
		return 3;
	    }
	}

	if (tags.containsKey("motor_vehicle")) {
	    restCount++;

	    String access = tags.get("motor_vehicle");
	    if (access.equals("yes")) {
		return 1;
	    } else if (access.equals("no")) {
		return 0;
	    } else if (access.equals("permissive")) {
		return 1;
	    } else if (access.equals("official")) {
		return 1;
	    } else if (access.equals("private")) {
		return 2;
	    } else if (access.equals("destination")) {
		return 2;
	    } else if (access.equals("agricultural")) {
		return 0;
	    } else if (access.equals("forestry")) {
		return 0;
	    } else if (access.equals("unknown")) {
		return 2;
	    } else if (access.equals("designated")) {
		return 1;
	    } else if (access.equals("delivery")) {
		return 3;
	    }
	}

	if (osmWay.getTags().get("highway").equals("service")) {
	    return 2;
	}
	return 1;
    }

    public int getSpeedSet() {
	return speedSet;
    }

    private String getStreetcategory(OsmWay osmWay) {
	if (osmWay.getTags().containsKey("highway")) {
	    return osmWay.getTags().get("highway");
	}

	return "";
    }

    /**
     * Gets the name of the street.
     * 
     * @param osmWay
     *            The original way.
     * @return The name.
     */
    private String getStreetName(OsmWay osmWay) {

	// read the name. process step by step from general to specific name
	// tags.
	
	if (osmWay.getTags().get("ref") != null) {

	    if (osmWay.getTags().get("name") != null) {
		conversionLogger.getScorer().reportNamedStreet("ref & name",
			osmWay.getTags().get("highway"));
	    }

	    conversionLogger.getScorer().reportNamedStreet("ref",
		    osmWay.getTags().get("highway"));
	    return osmWay.getTags().get("ref");
	} else if (osmWay.getTags().get("name") != null) {
	    conversionLogger.getScorer().reportNamedStreet("name",
		    osmWay.getTags().get("highway"));
	    return osmWay.getTags().get("name");
	} else if (osmWay.getTags().get("alt_name") != null) {
	    conversionLogger.getScorer().reportNamedStreet("alt_name",
		    osmWay.getTags().get("highway"));
	    return osmWay.getTags().get("alt_name");
	} else if (osmWay.getTags().get("int_name") != null) {
	    conversionLogger.getScorer().reportNamedStreet("int_name",
		    osmWay.getTags().get("highway"));
	    return osmWay.getTags().get("int_name");
	} else if (osmWay.getTags().get("nat_name") != null) {
	    conversionLogger.getScorer().reportNamedStreet("nat_name",
		    osmWay.getTags().get("highway"));
	    return osmWay.getTags().get("nat_name");
	} else if (osmWay.getTags().get("reg_name") != null) {
	    conversionLogger.getScorer().reportNamedStreet("reg_name",
		    osmWay.getTags().get("highway"));
	    return osmWay.getTags().get("reg_name");
	} else if (osmWay.getTags().get("loc_name") != null) {
	    conversionLogger.getScorer().reportNamedStreet("loc_name",
		    osmWay.getTags().get("highway"));
	    return osmWay.getTags().get("loc_name");
	} else if (osmWay.getTags().containsKey("unnamed")
		|| osmWay.getTags().containsKey("unsigned")
		|| osmWay.getTags().containsKey("name:absent")) {
	    conversionLogger.getScorer().reportNamedStreet("unnamed",
		    osmWay.getTags().get("highway"));
	    return "unnamed";
	} else if (osmWay.getTags().get("highway").endsWith("link")) {

	    // if this is a link to a motorway check id a node contains
	    // information about name and number of the exit.

	    List<OsmNode> nodes = osmWay.getNodes();

	    for (OsmNode node : nodes) {
		if (node.getTags().containsKey("highway")) {

		    if (node.getTags().get("highway")
			    .equals("motorway_junction")) {

			String name = "Abfahrt";

			boolean ref = false;
			boolean nameSet = false;

			if (node.getTags().containsKey("ref")) {
			    name += " " + node.getTags().get("ref");
			    ref = true;
			}
			if (node.getTags().containsKey("name")) {
			    name += ": " + node.getTags().get("name");
			    nameSet = true;
			}

			if (nameSet && ref) {
			    conversionLogger.getScorer().reportNamedStreet(
				    "ref & name",
				    osmWay.getTags().get("highway"));
			} else if (nameSet) {
			    conversionLogger.getScorer().reportNamedStreet(
				    "name", osmWay.getTags().get("highway"));
			} else if (ref) {
			    conversionLogger.getScorer().reportNamedStreet(
				    "ref", osmWay.getTags().get("highway"));
			} else {
			    conversionLogger.getScorer().reportUnnamedStreet(
				    osmWay.getTags().get("highway"));
			    return "nameMissing";
			}

			return name;
		    }
		}
	    }

	    conversionLogger.getScorer().reportUnnamedStreet(
		    osmWay.getTags().get("highway"));
	    return "nameMissing";

	} else {
	    conversionLogger.getScorer().reportUnnamedStreet(
		    osmWay.getTags().get("highway"));
	    return "nameMissing";
	}
    }

    /**
     * Derives the street-type-id ({@link Link}) from the
     * OpenStreetMap-street-categories. Unknown OSM-street-categories are saved
     * in {@code unreadHighways} and logged as a warning. As it's unlikely that
     * there is a hierarchically high category which is unknown, the default
     * value for those streets is {@code street-type = 5}.
     * 
     * @param osmWay
     *            the {@link OsmWay} which's street-type is searched
     * @return The street-type-id (see {@link Link})
     */
    private int getStreettype(OsmWay osmWay) {
	String highway = osmWay.getTags().get("highway");

	int streettype = highwayDefaults.getStreetCategory(highway);

	if (streettype == 11) {

	    conversionLogger.reportUnknownHighway(highway);
	    streettype = 10;

	    if (firstTimeUntyped) {
		logInfo("Map contains currently untyped street.");
		firstTimeUntyped = false;
	    }
	}

	return streettype;
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

    /**
     * 
     * Remove nodes from streetmap which are not referenced by any link.
     * 
     */
    private void removeUnusedNodes() {
	// creating list of all used nodes
	ArrayList<String> linkedNodes = new ArrayList<String>();
	for (Link link : links.values()) {
	    linkedNodes.add(link.getStartNodeId());
	    linkedNodes.add(link.getEndNodeId());
	}

	// indirectly removing unused nodes

	for (String id : linkedNodes) {
	    nodes.put(id, tempNodes.get(id));
	}

	conversionLogger.logInfo((tempNodes.size() - nodes.size())
		+ " unused nodes deleted!");
	conversionLogger.logInfo("new node-count: " + nodes.size());
    }

    /**
     * 
     * Revers the order of the nodes of an osmWay.
     * 
     * @param way
     * @return
     */
    private OsmWay reverseWay(OsmWay way) {
	List<OsmNode> rightNodes = new LinkedList<OsmNode>();

	for (OsmNode node : way.getNodes()) {
	    rightNodes.add(0, node);
	}

	way.setNodes(rightNodes);

	return way;
    }

    /**
     * Splits an {@link OsmWay} onto several sub-{@link Link}s to eliminate
     * between-nodes.
     * 
     * @param osmWay
     *            {@link OsmWay} to be splitted.
     * @param firstId
     *            Index for the first Id (necessary for a unique Id)
     * @param link
     *            The {@link Link} which already has max. speed, restrictions
     *            etc. set.
     */
    private Map<String, Link> splitLinks(OsmWay osmWay, int firstId, Link link) {

	Map<String, Link> links = new HashMap<String, Link>();
	List<OsmNode> wayNodes = osmWay.getNodes();

	for (int i = 0; i < wayNodes.size() - 1; i++) {
	    Link newLink = new Link();

	    // set Id
	    newLink.setId("" + (firstId));
	    firstId++;

	    // get start- and end-node for the current part
	    OsmNode startNode = wayNodes.get(i);
	    OsmNode endNode = wayNodes.get(i + 1);

	    // set them to the current sub-link
	    newLink.setStartNodeId(startNode.getId());
	    newLink.setEndNodeId(endNode.getId());

	    // get the coordinates of those nodes
	    double startLat = osmNodes.get(startNode.getId()).getLat();
	    double startLon = osmNodes.get(startNode.getId()).getLon();
	    double endLat = osmNodes.get(endNode.getId()).getLat();
	    double endLon = osmNodes.get(endNode.getId()).getLon();

	    // set the coords to the nodes
	    startNode.setLat(startLat);
	    startNode.setLon(startLon);
	    endNode.setLat(endLat);
	    endNode.setLon(endLon);

	    // set the coords to the links
	    newLink.setStartLat(startLat);
	    newLink.setStartLon(startLon);
	    newLink.setEndLat(endLat);
	    newLink.setEndLon(endLon);

	    // calc the links length
	    newLink.setLength(CoordinateHelper.calcDistanceBetweenNodes(
		    startNode, endNode));

	    // set name, maxSpeed, streetType, restrictions
	    // those values are inherited from the original OsmWay!
	    newLink.setName(link.getName());
	    newLink.setSpeed(link.getSpeed());
	    newLink.setStreetType(link.getStreetType());
	    newLink.setStreetCategory(link.getStreetCategory());
	    newLink.setRestrictions(link.getRestrictions());
	    newLink.setOneWay(link.isOneWay());
	    newLink.setNumberOfLanes(link.getNumberOfLanes());
	    newLink.setDefaultless(link.isDefaultless());

	    // if it is not one way create reverse direction

	    if (!link.isOneWay()) {
		Link newLinkReverse = new Link();

		newLinkReverse.setId("" + (firstId));
		firstId++;

		newLinkReverse.setStartNodeId(endNode.getId());
		newLinkReverse.setEndNodeId(startNode.getId());

		newLinkReverse.setStartLat(endLat);
		newLinkReverse.setStartLon(endLon);
		newLinkReverse.setEndLat(startLat);
		newLinkReverse.setEndLon(startLon);

		newLinkReverse.setLength(newLink.getLength());

		// set name, maxSpeed, streetType, restrictions
		// those values are inherited from the original OsmWay!
		newLinkReverse.setName(link.getName());
		newLinkReverse.setSpeed(link.getSpeed());
		newLinkReverse.setStreetType(link.getStreetType());
		newLinkReverse.setStreetCategory(link.getStreetCategory());
		newLinkReverse.setRestrictions(link.getRestrictions());
		newLinkReverse.setOneWay(false);
		newLinkReverse.setNumberOfLanes(link.getNumberOfLanes());
		newLinkReverse.setDefaultless(link.isDefaultless());

		links.put(newLinkReverse.getId(), newLinkReverse);
	    }

	    // collect all created links and ...
	    links.put(newLink.getId(), newLink);
	}

	// ... return them
	return links;
    }
}
