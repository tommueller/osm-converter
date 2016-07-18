package osmConverter.data;

import java.util.Map;

/**
 * Class containing complete street-map.
 * 
 * @author Tom Müller
 * 
 */
public class StreetMap {

    private Map<String, Link> links;
    private Map<Integer, Street> streets;
    private Map<String, Node> nodes;

    public Map<String, Link> getLinks() {
	return links;
    }

    public Map<String, Node> getNodes() {
	return nodes;
    }

    public void addLink(Link link) {
	this.links.put(link.getId(), link);
    }

    public void addNode(Node node) {
	this.nodes.put(node.getId(), node);
    }

    public void setLinks(Map<String, Link> links) {
	this.links = links;
    }

    public void setNodes(Map<String, Node> nodes) {
	this.nodes = nodes;
    }

    /**
     * @return The smallest latitude value of all links.
     */
    public double getSmallestLat() {

	double smallLat = 9999999999.;

	for (Link link : this.getLinks().values()) {
	    if (link.getStartLat() < smallLat) {
		smallLat = link.getStartLat();
	    }
	    if (link.getEndLat() < smallLat) {
		smallLat = link.getEndLat();
	    }
	}

	return smallLat;
    }

    /**
     * @return The biggest latitude value of all links.
     */
    public double getBiggestLat() {

	double bigLat = 0.;

	for (Link link : this.getLinks().values()) {
	    if (link.getStartLat() > bigLat) {
		bigLat = link.getStartLat();
	    }
	    if (link.getEndLat() > bigLat) {
		bigLat = link.getEndLat();
	    }
	}

	return bigLat;
    }

    /**
     * @return The smallest longitude value of all links.
     */
    public double getSmallestLon() {

	double smallLon = 9999999999.;

	for (Link link : this.getLinks().values()) {
	    if (link.getStartLon() < smallLon) {
		smallLon = link.getStartLon();
	    }
	    if (link.getEndLon() < smallLon) {
		smallLon = link.getEndLon();
	    }
	}

	return smallLon;
    }

    /**
     * @return The biggest longitude value of all links.
     */
    public double getBiggestLon() {

	double bigLon = 0.;

	for (Link link : this.getLinks().values()) {
	    if (link.getStartLon() > bigLon) {
		bigLon = link.getStartLon();
	    }
	    if (link.getEndLon() > bigLon) {
		bigLon = link.getEndLon();
	    }
	}

	return bigLon;
    }

    public void setStreets(Map<Integer, Street> streets) {
	this.streets = streets;
    }

    public Map<Integer, Street> getStreets() {
	return streets;
    }
}
