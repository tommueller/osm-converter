package osmConverter.beans;

import java.util.ArrayList;

/**
 * 
 * ArrayList extend with methods to get first and last element.
 * 
 * @author Tom Müller
 */
public class NodeList<E> extends ArrayList<E> {

    private static final long serialVersionUID = 4667876090507194008L;

    public E getFirstElement() {
	return this.get(0);
    }

    public E getLastElement() {
	return this.get(this.size() - 1);
    }

}
