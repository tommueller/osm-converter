/**
 * 
 */
package osmConverter.data;

import java.util.Map;

/**
 * 
 * Class providing basis functionality needed by every kind of osm-entity.
 * 
 * @author Tom Müller
 * @version 25.07.2010
 */
public abstract class OsmObject {

    private String id;
    private Tags tags;
    private String role = "";

    public String getRole() {
	return role;
    }

    public void setRole(String role) {
	this.role = role;
    }

    public Map<String, String> getTags() {
	return this.tags.getTags();
    }
    
    public void setTags(Map<String, String> tags) {
	this.tags.setTags(tags);
    }

    public OsmObject(String id) {
	this.id = id;
	tags = new Tags();
    }

    /**
     * @return the id
     */
    public String getId() {
	return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
	this.id = id;
    }

    public void addTag(String key, String value) {
	tags.put(key, value);
    }

}
