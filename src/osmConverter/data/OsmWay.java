/**
 * 
 */
package osmConverter.data;

import java.util.List;

import osmConverter.beans.NodeList;

/**
 * 
 * Class representing an OSM way entity.
 * 
 * @author Tom Müller
 * @version 25.07.2010
 */
public class OsmWay extends OsmObject implements Cloneable {

    private List<OsmNode> nodes;

    public List<OsmNode> getNodes() {
	return nodes;
    }

    public OsmWay(String id) {
	super(id);
    }

    public void addNode(OsmNode node) {
	if (nodes == null) {
	    nodes = new NodeList<OsmNode>();
	}
	this.nodes.add(node);
    }

    public void addNodes(List<OsmNode> nodes) {
	if (this.nodes == null) {
	    this.nodes = new NodeList<OsmNode>();
	}
	this.nodes.addAll(nodes);
    }

    public void setNodes(List<OsmNode> rightNodes) {
	this.nodes = rightNodes;
    }

    /**
     * Create a similiar clone of the object.
     */
    public OsmWay clone() {
	try {
	    return (OsmWay) super.clone();
	} catch (CloneNotSupportedException e) {
	    e.printStackTrace();
	}
	return null;
    }

}
