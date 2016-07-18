/**
 * 
 */
package osmConverter.data;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * List of all tags owned by an osm-entity.
 * 
 * @author Tom Müller
 * @version 25.07.2010
 */
public class Tags {

    private Map<String, String> tags = new HashMap<String, String>();

    /**
     * @return the tags
     */
    public Map<String, String> getTags() {
	return tags;
    }

    public void put(String key, String value) {
	tags.put(key, value);
    }
    
    public void putAll(Map<String, String> newTags) {
	tags.putAll(newTags);
    }

    public void setTags(Map<String, String> tags) {
	this.tags = tags;
    }

}
