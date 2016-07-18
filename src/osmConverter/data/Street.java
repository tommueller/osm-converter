package osmConverter.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Similar to Link-class, but representing an aggregation of links between
 * crossings.
 * 
 * @author Tom Müller
 * 
 */
public class Street {

    /**
     * Its unique ID
     */
    private int id;
    /**
     * The type of the edge
     */
    private int streetType;
    /**
     * The maximum speed allowed on this edge
     */
    private int speed;
    /**
     * The name of the edge
     */
    private String name;
    /**
     * The length of the edge
     */
    private double length;

    private double startLat;
    private double endLat;
    private double startLon;
    private double endLon;

    private String startNodeId;
    private String endNodeId;

    public String getStartNodeId() {
	return startNodeId;
    }

    public void setStartNodeId(String startNodeId) {
	this.startNodeId = startNodeId;
    }

    public String getEndNodeId() {
	return endNodeId;
    }

    public void setEndNodeId(String endNodeId) {
	this.endNodeId = endNodeId;
    }

    private List<String> followingStreets = new ArrayList<String>();
    private List<String> leadingStreets = new ArrayList<String>();
    private List<String> edgeIDs = new ArrayList<String>();

    /**
     * @return the streetType
     */
    public int getStreetType() {
	return streetType;
    }

    /**
     * @param streetType
     *            the streetType to set
     */
    public void setStreetType(int streetType) {
	this.streetType = streetType;
    }

    /**
     * @return the speed
     */
    public int getSpeed() {
	return speed;
    }

    /**
     * @param speed
     *            the speed to set
     */
    public void setSpeed(int speed) {
	this.speed = speed;
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return the length
     */
    public double getLength() {
	return length;
    }

    /**
     * @param length
     *            the length to set
     */
    public void setLength(double length) {
	this.length = length;
    }

    /**
     * @return the startLat
     */
    public double getStartLat() {
	return startLat;
    }

    /**
     * @param startLat
     *            the startLat to set
     */
    public void setStartLat(double startLat) {
	this.startLat = startLat;
    }

    /**
     * @return the endLat
     */
    public double getEndLat() {
	return endLat;
    }

    /**
     * @param d
     *            the endLat to set
     */
    public void setEndLat(double d) {
	this.endLat = d;
    }

    /**
     * @return the startLon
     */
    public double getStartLon() {
	return startLon;
    }

    /**
     * @param startLon
     *            the startLon to set
     */
    public void setStartLon(double startLon) {
	this.startLon = startLon;
    }

    /**
     * @return the endLon
     */
    public double getEndLon() {
	return endLon;
    }

    /**
     * @param endLon
     *            the endLon to set
     */
    public void setEndLon(double endLon) {
	this.endLon = endLon;
    }

    /**
     * @return the id
     */
    public int getId() {
	return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
	this.id = id;
    }

    /**
     * @return the edgeIDs
     */
    public List<String> getEdgeIDs() {
	return edgeIDs;
    }

    /**
     * @param edgeIDs
     *            the edgeIDs to set
     */
    public void setEdgeIDs(List<String> edgeIDs) {
	this.edgeIDs = edgeIDs;
    }

    /**
     * @return the followingStreets
     */
    public List<String> getFollowingStreets() {
	return followingStreets;
    }

    /**
     * @param followingStreets
     *            the followingStreets to set
     */
    public void setFollowingStreets(List<String> followingStreets) {
	this.followingStreets = followingStreets;
    }

    /**
     * @return the leadingStreets
     */
    public List<String> getLeadingStreets() {
	return leadingStreets;
    }

    /**
     * @param leadingStreets
     *            the leadingStreets to set
     */
    public void setLeadingStreets(List<String> leadingStreets) {
	this.leadingStreets = leadingStreets;
    }

}
