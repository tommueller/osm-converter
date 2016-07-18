package osmConverter.logic;

import java.util.Map;

import org.xml.sax.SAXException;

import osmConverter.data.Link;
import osmConverter.data.Node;
import osmConverter.data.OsmNode;
import osmConverter.data.OsmWay;
import osmConverter.data.StreetMap;

public interface IConverter {

    public Map<String, Link> convertHighway(OsmWay osmWay, int firstId);

    public StreetMap convertMap(String filename, boolean b) throws SAXException, Exception;

    public StreetMap convertMap(String osmFile, String outFile)
	    throws SAXException, Exception;
    
    public Node convertNode(OsmNode osmNode);
    
    public ConversionEventLogger getConversionLogger();

}
