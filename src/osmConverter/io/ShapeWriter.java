package osmConverter.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultAttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;

import osmConverter.data.Link;
import osmConverter.data.Street;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * This class can write two kinds of shape-files.
 * LinkShape is representing the geometric-correct-map.
 * StreetShape is the minimal-map.
 * @author Tom M�ller
 *
 */
public class ShapeWriter {

    private GeometryFactory geometryFactory = new GeometryFactory();
    private FeatureType featureType;

    public void writeLinkShape(Map<String, Link> links, String filename)
	    throws IOException {

	if (links != null && filename != null && !filename.equals("")) {
	    ArrayList<Feature> features = createLinkFeatures(links);
	    writeFeatures(features, filename);
	}
    }

    public void writeStreetShape(Map<Integer, Street> links, String filename)
	    throws IOException {

	if (links != null && filename != null && !filename.equals("")) {
	    ArrayList<Feature> features = createStreetFeatures(links);
	    writeFeatures(features, filename);
	}
    }

    private void writeFeatures(ArrayList<Feature> features, String filename)
	    throws IOException {

	URL fileURL = (new File(filename)).toURL();
	ShapefileDataStore datastore = new ShapefileDataStore(fileURL);
	Feature feature = features.iterator().next();
	datastore.createSchema(feature.getFeatureType());

	FeatureStore featureStore = (FeatureStore) (datastore
		.getFeatureSource(feature.getFeatureType().getTypeName()));
	FeatureReader aReader = DataUtilities.reader(features);

	featureStore.addFeatures(aReader);
    }

    private Feature getLinkFeature(Street link) {
	Coordinate[] coords = new Coordinate[2];

	Coordinate startCoord = new Coordinate(link.getStartLon(),
		link.getStartLat());
	Coordinate endCoord = new Coordinate(link.getEndLon(), link.getEndLat());

	coords[0] = startCoord;
	coords[1] = endCoord;

	LineString ls = this.geometryFactory.createLineString(coords);

	Object[] attribs = new Object[7];
	attribs[0] = ls;
	attribs[1] = link.getId();
	attribs[2] = link.getName();
	attribs[3] = (link.getName().equals("")) ? 0 : 1;
	attribs[4] = link.getStreetType();
	attribs[5] = 0;
	attribs[6] = link.getLength();

	// Hier die weiteren Attribute ausfüllen

	try {
	    return this.featureType.create(attribs);
	} catch (IllegalAttributeException e) {
	    throw new RuntimeException(e);
	}
    }

    private Feature getLinkFeature(Link link) {

	Coordinate[] coords = new Coordinate[2];

	Coordinate startCoord = new Coordinate(link.getStartLon(),
		link.getStartLat());
	Coordinate endCoord = new Coordinate(link.getEndLon(), link.getEndLat());

	coords[0] = startCoord;
	coords[1] = endCoord;

	LineString ls = this.geometryFactory.createLineString(coords);

	boolean defaultless = link.isDefaultless();

	if (link.getName().equals("")) {
	    defaultless = false;
	}

	Object[] attribs = new Object[7];
	attribs[0] = ls;
	attribs[1] = link.getId();
	attribs[2] = link.getName();
	attribs[3] = (link.getName().equals("")) ? 0 : 1;
	attribs[4] = link.getStreetType();
	attribs[5] = defaultless ? 1 : 0;
	attribs[6] = link.getLength();

	// Hier die weiteren Attribute ausfüllen

	try {
	    return this.featureType.create(attribs);
	} catch (IllegalAttributeException e) {
	    throw new RuntimeException(e);
	}

    }

    private ArrayList<Feature> createLinkFeatures(Map<String, Link> links) {
	this.initLinkFeatureType();

	ArrayList<Feature> f = new ArrayList<Feature>();
	for (Link link : links.values()) {
	    if (link.getStreetType() < 5) {
		f.add(getLinkFeature(link));
	    }
	}

	// for (Link link : links.values()) {
	// f.add(getLinkFeature(link));
	// }

	return f;
    }

    private ArrayList<Feature> createStreetFeatures(Map<Integer, Street> links) {
	this.initLinkFeatureType();

	ArrayList<Feature> f = new ArrayList<Feature>();
	for (Street link : links.values()) {
	    if (link.getStreetType() < 5) {
		f.add(getLinkFeature(link));
	    }
	}

	// for (Link link : links.values()) {
	// f.add(getLinkFeature(link));
	// }

	return f;
    }

    /**
     * Ein FeatureType ist sozusagen die Datensatzbeschreibung für das, was
     * visualisiert werden soll. Schreiben Sie in das Array attribs hier Ihre
     * eigenen Datenfelder dazu.
     * 
     */
    private void initLinkFeatureType() {
	AttributeType[] attribs = new AttributeType[7];
	attribs[0] = DefaultAttributeTypeFactory.newAttributeType("LineString",
		LineString.class);
	attribs[1] = AttributeTypeFactory.newAttributeType("ID", String.class);
	attribs[2] = AttributeTypeFactory
		.newAttributeType("Name", String.class);
	attribs[3] = AttributeTypeFactory.newAttributeType("Name-Set",
		Integer.class);
	attribs[4] = AttributeTypeFactory.newAttributeType("Streettype",
		Integer.class);
	attribs[5] = AttributeTypeFactory.newAttributeType("Defaultles",
		Integer.class);
	attribs[6] = AttributeTypeFactory.newAttributeType("Length",
		Double.class);

	// Hier weitere Attribute anlegen

	try {
	    this.featureType = FeatureTypeBuilder.newFeatureType(attribs,
		    "link");
	} catch (FactoryRegistryException e) {
	    e.printStackTrace();
	} catch (SchemaException e) {
	    e.printStackTrace();
	}
    }
}
