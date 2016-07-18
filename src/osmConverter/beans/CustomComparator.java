package osmConverter.beans;

import java.io.Serializable;
import java.util.Comparator;

/**
 * 
 * Comparator to sort Strings containing Numbers in numerical order.
 * 
 * @author Tom Müller
 * 
 */
public class CustomComparator implements Comparator<String>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5142717387157670017L;

    @Override
    public int compare(String arg0, String arg1) {
	return Integer.valueOf(arg0).compareTo(Integer.valueOf(arg1));
    }

}
