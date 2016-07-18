package osmConverter.logic;

import osmConverter.data.OsmNode;

public class CoordinateHelper {

    /**
     * Calculates the distance in meter between to nodes. The formula is derived
     * from "Seitencosinussatz".
     * 
     * @param startNode
     *            Node where the way begins.
     * @param endNode
     *            Node where the way ends.
     * @return Distance in meters.
     */
    public static double calcDistanceBetweenNodes(OsmNode startNode,
	    OsmNode endNode) {
	return Math.acos(Math.sin(endNode.getLat() / 180. * Math.PI)
		* Math.sin(startNode.getLat() / 180. * Math.PI)
		+ Math.cos(endNode.getLat() / 180. * Math.PI)
		* Math.cos(startNode.getLat() / 180. * Math.PI)
		* Math.cos(endNode.getLon() / 180. * Math.PI
			- startNode.getLon() / 180. * Math.PI)) * 6380. // ca.
									// der
									// Erdradius
	* 1000; // Umrechnung von km in m
    }

    /**
     * Calculates the distance in meter between to coordinates. The formula is
     * derived from "Seitencosinussatz".
     * 
     * @param startNode
     *            Node where the way begins.
     * @param endNode
     *            Node where the way ends.
     * @return Distance in meters.
     */
    public static double calcDistanceBetweenCoords(double startLat,
	    double startLon, double endLat, double endLon) {

	if (startLat == endLat && startLon == endLon) {
	    return 0;
	}

	return Math
		.acos(Math.sin(endLat / 180. * Math.PI)
			* Math.sin(startLat / 180. * Math.PI)
			+ Math.cos(endLat / 180. * Math.PI)
			* Math.cos(startLat / 180. * Math.PI)
			* Math.cos(endLon / 180. * Math.PI - startLon / 180.
				* Math.PI)) * 6380. // ca.
						    // der
						    // Erdradius
	* 1000; // Umrechnung von km in m
    }

}
