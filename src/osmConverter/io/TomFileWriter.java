package osmConverter.io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.TreeMap;

import osmConverter.beans.CustomComparator;
import osmConverter.data.Link;
import osmConverter.data.Node;
import osmConverter.data.StreetMap;

/**
 * This class offers functionality to write the text-file representing the map
 * which contains all necessary attributes!
 * 
 * @author Tom Müller
 * 
 */
public class TomFileWriter {

    /**
     * Write the text-file representing the map containing all necessary
     * attributes.
     * 
     * @param outFile
     *            Name of the created file.
     * @param streetMap
     *            The streetMap which shall be written.
     * @throws IOException
     */
    public void writeTomFile(String outFile, StreetMap streetMap)
	    throws IOException {
	try {
	    this.writeTomFile(outFile, streetMap, "\t");
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    return;
	}
    }

    /**
     * Write the text-file representing the map containing all necessary
     * attributes.
     * 
     * @param outFile
     *            Name of the created file.
     * @param streetMap
     *            The streetMap which shall be written.
     * @param separator
     *            The character which separates different entries.
     * @throws IOException
     */
    public void writeTomFile(String outFile, StreetMap streetMap,
	    String separator) throws IOException {
	try {
	    this.writeTomFile(outFile, streetMap, separator, false);
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    return;
	}
    }

    /**
     * Write the text-file representing the map containing all necessary
     * attributes.
     * 
     * @param outFile
     *            Name of the created file.
     * @param streetMap
     *            The streetMap which shall be written. * @param separator The
     *            character which separates different entries.
     * @param separator
     *            The character which separates different entries.
     * @param sorted
     *            Indicates whether the edge entities of the outcoming file
     *            shall be sorted or not.
     * @throws IOException
     */
    public void writeTomFile(String outFile, StreetMap streetMap,
	    String separator, boolean sorted) throws IOException {
	try {
	    FileWriter fw = new FileWriter("output/" + outFile, false);

	    // if sorted add to a treemap which's comparator sorts after id
	    if (sorted) {
		Comparator<String> c = new CustomComparator();
		TreeMap<String, Link> tm = new TreeMap<String, Link>(c);
		tm.putAll(streetMap.getLinks());

		for (Link link : tm.values()) {
		    String name = link.getName();
		    if (name.equals(""))
			name = "unnamed";

		    fw.write(link.getId() + separator + link.getStreetType()
			    + separator + link.getSpeed() + separator + name
			    + separator + link.getStartNodeId() + separator
			    + (int) (link.getStartLon() * 100000) + separator
			    + (int) (link.getStartLat() * 100000) + separator
			    + link.getEndNodeId() + separator
			    + (int) (link.getEndLon() * 100000) + separator
			    + (int) (link.getEndLat() * 100000) + separator
			    + link.getLength() + separator
			    + link.getRestrictions() + separator
			    + link.isOneWay() + separator
			    + link.getNumberOfLanes() + separator
			    + link.getFollowingLinks().size() + separator
			    + link.getLeadingLinks().size());

		    for (String id : link.getFollowingLinks()) {
			// fw.write(separator + (Integer.parseInt(id) - 1));
			fw.write(separator + id);
		    }
		    for (String id : link.getLeadingLinks()) {
			// fw.write(separator + (Integer.parseInt(id) - 1));
			fw.write(separator + id);
		    }

		    fw.write("\r\n");
		}
	    } else {
		for (Link link : streetMap.getLinks().values()) {
		    String name = link.getName();
		    if (name.equals(""))
			name = "unnamed";

		    fw.write(link.getId() + separator + link.getStreetType()
			    + separator + link.getSpeed() + separator + name
			    + separator + link.getStartNodeId() + separator
			    + (int) (link.getStartLon() * 100000) + separator
			    + (int) (link.getStartLat() * 100000) + separator
			    + link.getEndNodeId() + separator
			    + (int) (link.getEndLon() * 100000) + separator
			    + (int) (link.getEndLat() * 100000) + separator
			    + link.getLength() + separator
			    + link.getRestrictions() + separator
			    + link.isOneWay() + separator
			    + link.getFollowingLinks().size() + separator
			    + link.getLeadingLinks().size());

		    for (String id : link.getFollowingLinks()) {
			// fw.write(separator + (Integer.parseInt(id) - 1));
			fw.write(separator + id);
		    }
		    for (String id : link.getLeadingLinks()) {
			// fw.write(separator + (Integer.parseInt(id) - 1));
			fw.write(separator + id);
		    }

		    fw.write("\r\n");
		}
	    }

	    fw.write("### nodes ###\r\n");

	    for (Node node : streetMap.getNodes().values()) {
		fw.write(node.getId() + separator
			+ (int) (node.getLat() * 100000) + separator
			+ (int) (node.getLon() * 100000) + "\r\n");
	    }
	    fw.flush();
	    fw.close();
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    return;
	}
    }
}
