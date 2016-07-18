package osmConverter.data;

/**
 * 
 * Class representing a node.
 * 
 * @author Tom Müller
 * @version 26.07.2010
 */
public class Node {

    private String id;
    private double lat;
    private double lon;

    public double getLat() {
	return lat;
    }

    public void setLat(double d) {
	this.lat = d;
    }

    public double getLon() {
	return lon;
    }

    public void setLon(double d) {
	this.lon = d;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }
}
