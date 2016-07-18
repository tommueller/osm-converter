package osmConverter.data;

/**
 * Class representing an osm-restriction-relation.
 * @author Tom Müller
 *
 */
public class Restriction {

    private String from = "";
    private String to = "";
    private String via = "";

    public String getVia() {
	return via;
    }

    public void setVia(String via) {
	this.via = via;
    }

    private boolean temporary = false;
    private String fromTime = "";
    private String toTime = "";
    private String fromDay = "";
    private String toDay = "";
    private String type = "";
    private String id = "";
    private boolean fromIsConverted = false;
    private boolean toIsConverted = false;

    public boolean createRestriction(OsmRelation osmRelation) {
	try {
	    if (osmRelation.getTags().get("restriction").startsWith("no")) {
		this.setType("no");
	    } else if (osmRelation.getTags().get("restriction")
		    .startsWith("only")) {
		this.setType("only");
	    }
	    this.setId(osmRelation.getId());

	    for (OsmObject object : osmRelation.getMembers()) {
		if (object instanceof OsmWay) {
		    if (((OsmWay) object).getRole().equals("from")) {
			this.setFrom(object.getId());
		    } else if (((OsmWay) object).getRole().equals("to")) {
			this.setTo(object.getId());
		    } else if (((OsmWay) object).getRole().equals("via")) {

		    }

		    if (((OsmWay) object).getTags().containsKey("day_on")) {
			this.setTemporary(true);
			this.setFromDay(object.getTags().get("day_on"));
		    } else if (((OsmWay) object).getTags().containsKey(
			    "day_off")) {
			this.setTemporary(true);
			this.setToDay(object.getTags().get("day_off"));
		    } else if (((OsmWay) object).getTags().containsKey(
			    "hour_on")) {
			this.setTemporary(true);
			this.setFromTime(object.getTags().get("hour_on"));
		    } else if (((OsmWay) object).getTags().containsKey(
			    "hour_off")) {
			this.setTemporary(true);
			this.setToTime(object.getTags().get("hour_off"));
		    }
		}
	    }

	    if (!this.getFrom().equals("") && !this.getTo().equals("")) {
		return true;
	    } else {
		return false;
	    }
	} catch (Exception e) {
	    return false;
	}

    }

    public boolean isFromConverted() {
	return fromIsConverted;
    }

    public boolean isToConverted() {
	return toIsConverted;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public String getFromDay() {
	return fromDay;
    }

    public void setFromDay(String fromDay) {
	this.fromDay = fromDay;
    }

    public String getToDay() {
	return toDay;
    }

    public void setToDay(String toDay) {
	this.toDay = toDay;
    }

    public boolean isTemporary() {
	return temporary;
    }

    public void setTemporary(boolean temporary) {
	this.temporary = temporary;
    }

    public String getFromTime() {
	return fromTime;
    }

    public void setFromTime(String fromTime) {
	this.fromTime = fromTime;
    }

    public String getToTime() {
	return toTime;
    }

    public void setToTime(String toTime) {
	this.toTime = toTime;
    }

    public String getFrom() {
	return from;
    }

    public void setFrom(String from) {
	this.from = from;
    }

    public String getTo() {
	return to;
    }

    public void setTo(String to) {
	this.to = to;
    }

    public void setFromIsConverted(boolean b) {
	this.fromIsConverted = b;
    }

    public void setToIsConverted(boolean b) {
	this.toIsConverted = b;
    }

}
