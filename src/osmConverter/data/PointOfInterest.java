package osmConverter.data;

import java.util.Map;

public class PointOfInterest extends Node {
    private String type;
    Tags tags;

    public PointOfInterest() {
	super();
	tags = new Tags();
    }

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public Map<String, String> getTags() {
	return tags.getTags();
    }

    public void setTags(Tags tags) {
	this.tags = tags;
    }

    public void setTags(Map<String, String> tags) {
	this.tags.setTags(tags);
    }

    public void deriveType() {
	if (this.getTags().containsKey("railway")) {
	    if (this.getTags().get("railway").equals("station")
		    || this.getTags().get("railway").equals("halt")
		    || this.getTags().get("railway").equals("tram_stop")) {
		if (this.getTags().containsKey("station")) {
		    type = this.getTags().get("station") + "stop";
		} else {
		    type = this.getTags().get("railway");
		}
	    }
	} else if (this.getTags().containsKey("highway")) {
	    if (this.getTags().get("highway").equals("bus_stop")) {
		this.type = "bus_stop";
	    }
	} else if (this.getTags().containsKey("shop")) {
	    if (this.getTags().get("shop").equals("kiosk")) {
		this.type = "kiosk";
	    } else if (checkSpaeti()
		    && (this.getTags().get("shop").equals("beverages")
			    || this.getTags().get("shop").equals("convenience") || this
			    .getTags().get("shop").equals("newsagent"))) {
		this.type = "kiosk";
	    }
	} else if (this.getTags().containsKey("amenity")) {
	    if (this.getTags().get("amenity").equals("fuel")) {
		this.type = "fuel";
	    } else if (this.getTags().get("amenity").equals("bank")) {
		this.type = "bank";
	    } else if (this.getTags().get("amenity").equals("atm")) {
		this.type = "atm";
	    }
	} else if (this.getTags().containsKey("public_transport")) {
	    if (this.getTags().get("public_transport").equals("platform")) {
		this.type = "platform";
	    }
	}
    }

    private boolean checkSpaeti() {
	if (this.getTags().containsKey("name")) {
	    if (this.getTags().get("name").contains("Späti")
		    || this.getTags().get("name").contains("Spätkauf")
		    || this.getTags().get("name").contains("Spätverkauf")) {
		return true;
	    } else {
		return false;
	    }
	} else {
	    return false;
	}
    }
}
