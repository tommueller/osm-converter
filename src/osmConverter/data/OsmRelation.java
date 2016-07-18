/**
 * 
 */
package osmConverter.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Müller
 * @version 25.07.2010
 */
public class OsmRelation extends OsmObject {
    public List<OsmObject> getMembers() {
        return members;
    }

    public void setMembers(List<OsmObject> members) {
        this.members = members;
    }

    private List<OsmObject> members;
    private List<Node> nodes;
    private List<Link> links;

    public OsmRelation(String id) {
	super(id);
    }

    public void addMember(OsmObject member) {
	if (members == null) {
	    members = new ArrayList<OsmObject>();
	}
	members.add(member);
    }

    public void setNodes(List<Node> memberNodes) {
	this.nodes = memberNodes;
    }

    public void setLinks(List<Link> memberLinks) {
	this.links = memberLinks;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Link> getLinks() {
        return links;
    }
}
