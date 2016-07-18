package osmConverter.data;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MyArea extends Path2D.Double {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String Id = "";
    private String type = "";
    private boolean firsttime = true;

    private MyArea inner;

    List<OsmObject> assignedWays = new LinkedList<OsmObject>();
    List<OsmNode> assignedNodes = new LinkedList<OsmNode>();

    private Tags tags = new Tags();
    private List<java.lang.Double> lons;
    private List<java.lang.Double> lats;
    private List<String> nodess = new LinkedList<String>();

    public void addCoord(double lon, double lat) {
	// if its the first coordinate use moveTo otherwise use
	// lineTo
	if (firsttime) {
	    this.moveTo(lon, lat);
	    firsttime = false;
	} else {
	    this.lineTo(lon, lat);
	}

	// add also to custom lists, as Path2D does not grant access to the
	// coordinates later on
	if (this.lons != null) {
	    lons.add(lon);
	} else {
	    lons = new ArrayList<java.lang.Double>();
	    lons.add(lon);
	}

	if (this.lats != null) {
	    lats.add(lat);
	} else {
	    lats = new ArrayList<java.lang.Double>();
	    lats.add(lat);
	}
    }

    public void addTag(String key, String value) {
	tags.put(key, value);
    }

    public void addTags(Map<String, String> tags2) {
	tags.putAll(tags2);
    }

    public void close() {
	this.closePath();

	lons.add(lons.get(0));
	lats.add(lats.get(0));
    }

    /**
     * @param members
     * @return
     */
    public void createAreaFromRelation(OsmRelation osmObject,
	    Map<String, OsmWay> osmWays, Map<String, OsmNode> osmNodes) {

	List<OsmObject> originalMembers = ((OsmRelation) osmObject)
		.getMembers();

	sortMembers(originalMembers, osmWays, osmNodes);

	// merge all members to one polygon
	for (OsmObject member : assignedWays) {

	    if (member.getRole().equals("inner")) {
		if (inner == null) {
		    inner = new MyArea();
		}
		// if the current member is a way
		if (member instanceof OsmWay) {
		    OsmWay way = (OsmWay) member;

		    // add all nodes the way contains to the polygon
		    for (OsmNode node : way.getNodes()) {
			double lon = node.getLon();
			double lat = node.getLat();

			inner.addCoord(lon, lat);
		    }
		} else if (member instanceof OsmNode) {
		    OsmNode node = (OsmNode) member;

		    double lon = node.getLon();
		    double lat = node.getLat();

		    inner.addCoord(lon, lat);
		}
	    } else {
		// if the current member is a way
		if (member instanceof OsmWay) {
		    OsmWay way = (OsmWay) member;

		    // add all nodes the way contains to the polygon
		    for (OsmNode node : way.getNodes()) {
			double lon = node.getLon();
			double lat = node.getLat();

			this.addCoord(lon, lat);
			this.nodess.add(node.getId());
		    }
		} else if (member instanceof OsmNode) {
		    OsmNode node = (OsmNode) member;

		    double lon = node.getLon();
		    double lat = node.getLat();

		    this.addCoord(lon, lat);
		}
	    }
	}
    }

    public void createAreaFromWay(OsmWay osmWay) {
	for (OsmNode node : osmWay.getNodes()) {
	    this.addCoord(node.getLon(), node.getLat());
	}
    }

    public String getId() {
	return Id;
    }

    public MyArea getInner() {
	return inner;
    }

    public List<java.lang.Double> getLatCoordinates() {
	return lats;
    }

    public List<java.lang.Double> getLonCoordinates() {
	return lons;
    }

    public String getNodeByIndex(int i) {
	return nodess.get(i);
    }

    public List<OsmNode> getRelationsNodes() {
	return this.assignedNodes;
    }

    public Map<String, String> getTags() {
	return this.tags.getTags();
    }

    public String getType() {
	return type;
    }

    private OsmWay reverseWay(OsmWay way) {

	OsmWay newWay = way.clone();
	List<OsmNode> rightNodes = new LinkedList<OsmNode>();

	for (OsmNode node : way.getNodes()) {
	    rightNodes.add(0, node);
	}

	newWay.setNodes(rightNodes);

	return newWay;
    }

    public void setId(String id) {
	Id = id;
    }

    public void setType(String type) {
	this.type = type;
    }

    private void sortMembers(List<OsmObject> unassigned,
	    Map<String, OsmWay> osmWays, Map<String, OsmNode> nodes) {

	List<OsmWay> unassignedWays = new LinkedList<OsmWay>();

	for (OsmObject object : unassigned) {
	    if (object instanceof OsmWay) {
		OsmWay newWay = (OsmWay) object;
		if (osmWays.get(object.getId()) != null) {
		    newWay.addNodes(osmWays.get(object.getId()).getNodes());
		    unassignedWays.add(newWay);
		}
	    } else if (object instanceof OsmNode) {
		assignedNodes.add(nodes.get(((OsmNode) object).getId()));
	    }
	}

	OsmWay assi = null;

	int counter = 0;

	while (assignedWays.size() < unassigned.size()
		&& counter < unassigned.size()) {

	    List<OsmObject> tempAssignedWays = new LinkedList<OsmObject>();
	    List<OsmObject> tempOriginalAssignedWays = new LinkedList<OsmObject>();

	    try {
		tempAssignedWays.add(unassignedWays.get(counter));
		assi = (OsmWay) tempAssignedWays.get(0);
	    } catch (Exception e) {
	    }

	    for (int i = 0; i < unassignedWays.size(); i++) {
		if (!tempOriginalAssignedWays.contains(unassignedWays.get(i))) {
		    if (unassignedWays
			    .get(i)
			    .getNodes()
			    .get(0)
			    .getId()
			    .equals(assi.getNodes()
				    .get(assi.getNodes().size() - 1).getId())) {
			tempAssignedWays.add(unassignedWays.get(i));
			tempOriginalAssignedWays.add(unassignedWays.get(i));
			assi = unassignedWays.get(i);
			i = -1;

		    } else if (unassignedWays
			    .get(i)
			    .getNodes()
			    .get(unassignedWays.get(i).getNodes().size() - 1)
			    .getId()
			    .equals(assi.getNodes()
				    .get(assi.getNodes().size() - 1).getId())) {
			OsmWay reversedWay = reverseWay(unassignedWays.get(i));
			tempAssignedWays.add(reversedWay);

			tempOriginalAssignedWays.add(unassignedWays.get(i));

			assi = unassignedWays.get(i);
			i = -1;
		    }
		}
	    }
	    counter++;

	    if (tempAssignedWays.size() > assignedWays.size()) {
		assignedWays = tempAssignedWays;
	    }
	}
    }
}
