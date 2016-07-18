package osmConverter.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data-class describing a link of the map
 * 
 * @author Tom Müller
 */
public class Link implements Comparable<Link> {

    private boolean defaultless = true;

    private double endLat;

    private double endLon;
    /**
     * The node where the edge ends
     */

    private String endNodeId;

    private List<String> followingLinks = new ArrayList<String>();

    /**
     * Its unique ID
     */
    private String id;

    private List<String> leadingLinks = new ArrayList<String>();
    /**
     * The length of the edge
     */
    private double length;
    /**
     * The name of the edge
     */
    private String name;

    private int numberOfLanes = 1;

    private boolean oneWay = false;

    /**
     * This gets the restriction-code for the link. 0 = no through road. 1 = no
     * restrictions 2 = only destination and private traffic. 3 = only delivery
     * services are allowed. This comes often with {@code timeRestriction}
     * ...-tags.
     */

    private int restrictions;

    /**
     * The maximum speed allowed on this edge
     */
    private int speed;

    private double startLat;

    private double startLon;

    /**
     * The node where the edge starts
     */
    private String startNodeId;

    private String streetCategory = "";

    // / <summary>
    // / ID of carrying street
    // / </summary>
    private int streetID = -1;
    /**
     * The type of the edge
     */
    private int streetType;

    /**
     * Not really implemented yet ...
     */
    private int timeRestrictionEnd;
    private int timeRestrictionStart;

    @Override
    public int compareTo(Link otherLink) {
	return this.id.compareTo(otherLink.id);
    }

    public double getEndLat() {
	return endLat;
    }

    public double getEndLon() {
	return endLon;
    }

    /**
     * @return the end node
     */
    public String getEndNodeId() {
	return endNodeId;
    }

    public String getFollowEdge(int l) {
	return followingLinks.get(l);
    }

    public int getFollowEdgesCount() {
	return followingLinks.size();
    }

    public List<String> getFollowingLinks() {
	return followingLinks;
    }

    /**
     * @return the edgeID
     */
    public String getId() {
	return id;
    }

    public Object getLeadingEdge(int i) {
	return leadingLinks.get(i);
    }

    public int getLeadingEdgesCount() {
	return leadingLinks.size();
    }

    public List<String> getLeadingLinks() {
	return leadingLinks;
    }

    /**
     * @return the length
     */
    public double getLength() {
	return length;
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    public int getNumberOfLanes() {
	return this.numberOfLanes;
    }

    /**
     * This gets the restriction-code for the link. 0 = no through road. 1 = no
     * restrictions 2 = only destination and private traffic. 3 = only delivery
     * services are allowed. This comes often with {@code timeRestriction}
     * ...-tags.
     */
    public int getRestrictions() {
	return restrictions;
    }

    /**
     * @return the speed
     */
    public int getSpeed() {
	return speed;
    }

    public double getStartLat() {
	return startLat;
    }

    public double getStartLon() {
	return startLon;
    }

    /**
     * @return the start node
     */
    public String getStartNodeId() {
	return startNodeId;
    }

    public String getStreetCategory() {
	return streetCategory;
    }

    /**
     * @return the streetID
     */
    public int getStreetID() {
	return streetID;
    }

    /**
     * @return the street type
     */
    public int getStreetType() {
	return streetType;
    }

    public int getTimeRestrictionEnd() {
	return timeRestrictionEnd;
    }

    public int getTimeRestrictionStart() {
	return timeRestrictionStart;
    }

    public boolean isOneWay() {
	return oneWay;
    }

    public void setEndLat(double endLat) {
	this.endLat = endLat;
    }

    public void setEndLon(double endLon) {
	this.endLon = endLon;
    }

    /**
     * @param endNode
     *            the end node to set
     */
    public void setEndNodeId(String endNode) {
	this.endNodeId = endNode;
    }

    public void setFollowingLinks(List<String> followingLinks) {
	this.followingLinks = followingLinks;
    }

    /**
     * @param id2
     *            the edge ID to set
     */
    public void setId(String id) {
	this.id = id;
    }

    public void setLeadingLinks(List<String> leadingLinks) {
	this.leadingLinks = leadingLinks;
    }

    /**
     * @param length
     *            the length to set
     */
    public void setLength(double length) {
	this.length = length;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    public void setNumberOfLanes(int numberOfLanes) {
	this.numberOfLanes = numberOfLanes;
    }

    public void setOneWay(boolean oneWay) {
	this.oneWay = oneWay;
    }

    /**
     * This sets the restriction-code for the link. 0 = no through road. 1 = no
     * restrictions 2 = only destination and private traffic. 3 = only delivery
     * services are allowed. This comes often with {@code timeRestriction}
     * ...-tags.
     */
    
    public void setRestrictions(int restrictions) {
	this.restrictions = restrictions;
    }

    /**
     * @param speed
     *            the speed to set
     */
    public void setSpeed(int speed) {
	this.speed = speed;
    }

    public void setStartLat(double d) {
	this.startLat = d;
    }

    public void setStartLon(double d) {
	this.startLon = d;
    }

    /**
     * @param id
     *            the start node to set
     */
    public void setStartNodeId(String id) {
	this.startNodeId = id;
    }

    public void setStreetCategory(String streetCategory) {
	this.streetCategory = streetCategory;
    }

    /**
     * @param streetID
     *            the streetID to set
     */
    public void setStreetID(int streetID) {
	this.streetID = streetID;
    }

    /**
     * @param streetType
     *            the street type to set
     */
    public void setStreetType(int streetType) {
	this.streetType = streetType;
    }

    public void setTimeRestrictionEnd(int timeRestrictionEnd) {
	this.timeRestrictionEnd = timeRestrictionEnd;
    }

    public void setTimeRestrictionStart(int timeRestrictionStart) {
	this.timeRestrictionStart = timeRestrictionStart;
    }

    public void setDefaultless(boolean defaultless) {
	this.defaultless = defaultless;
    }

    public boolean isDefaultless() {
	return defaultless;
    }
}
