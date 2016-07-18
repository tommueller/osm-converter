/**
 * 
 */
package osmConverter.data;

/**
 * @author Tom Müller
 * @version 25.07.2010
 */
public class OsmNode extends OsmObject {

    private double lon;
    private double lat;

    public boolean isRealPOI() {
	if (this.getTags().containsKey("amenity")) {
	    return true;
	} else if (this.getTags().containsKey("railway")) {
	    if (this.getTags().get("railway").equals("station")
		    || this.getTags().get("railway").equals("halt")
		    || this.getTags().get("railway").equals("tram_stop")) {
		return true;
	    }
	}

	return false;
    }
    
    /**
     * @return the lon
     */
    public double getLon() {
	return lon;
    }

    /**
     * @param lon
     *            the lon to set
     */
    public void setLon(double lon) {
	this.lon = lon;
    }

    /**
     * @return the lat
     */
    public double getLat() {
	return lat;
    }

    /**
     * @param lat
     *            the lat to set
     */
    public void setLat(double lat) {
	this.lat = lat;
    }

    public OsmNode(String id, double lon, double lat) {
	super(id);
	this.lon = lon;
	this.lat = lat;
    }

    public OsmNode(String id) {
	super(id);
    }
}
