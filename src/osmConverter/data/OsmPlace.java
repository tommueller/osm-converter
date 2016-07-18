package osmConverter.data;


public class OsmPlace {

    private OsmNode node;
    private double radius;

    public OsmPlace(OsmNode node) {
	this.node = node;
	this.calcRadius();
    }

    private void calcRadius() {

	radius = (getRadius(node) == 0) ? getDefaultRadius(node.getTags().get(
		"place")) : getRadius(node);

    }
    

    private double getDefaultRadius(String placeType) {
	if (placeType.equals("city"))
	    return 10000.;
	if (placeType.equals("town"))
	    return 5000.;
	if (placeType.equals("village"))
	    return 1000.;
	if (placeType.equals("hamlet"))
	    return 300.;
	if (placeType.equals("suburb"))
	    return 300.;

	return 0;
    }

    private double getRadius(OsmNode node) {
	if (node.getTags().containsKey("radius")) {
	    return parseLengthInMeter(node.getTags().get("radius"));
	} else if (node.getTags().containsKey("place_radius")) {
	    return parseLengthInMeter(node.getTags().get("place_radius"));
	} else if (node.getTags().containsKey("diameter")) {
	    return parseLengthInMeter(node.getTags().get("diameter"));
	} else if (node.getTags().containsKey("place_diameter")) {
	    return parseLengthInMeter(node.getTags().get("place_diameter"));
	} else {
	    return 0;
	}
    }
    
    private double parseLengthInMeter(String radius) {
	if (radius.endsWith("km")) {
	    double rad = Double.parseDouble(radius.substring(0,
		    radius.length() - 3));
	    return rad * 1000.;
	} else if (radius.endsWith("m")) {
	    return Double.parseDouble(radius.substring(0, radius.length() - 2));
	} else {
	    try {
		return Double.parseDouble(radius.substring(0,
			radius.length() - 1));
	    } catch (Exception e) {
		System.out.println(e.getMessage());
		return 0;
	    }
	}
    }

    public OsmNode getNode() {
        return node;
    }

    public void setNode(OsmNode node) {
        this.node = node;
    }

    public double getRadius() {
       return radius;
    }
}
