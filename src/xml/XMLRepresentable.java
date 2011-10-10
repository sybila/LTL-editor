//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Specifies objects representable in XML form for usage with DOM XML parser.
 * 
 * @author Tomáš Vejpustek
 *
 */
public interface XMLRepresentable {
	/**
	 * @param document Parent document.
	 * @return XML representation of object.
	 */
	public Node toXML(Document document);
	
	/**
	 * @param document Parent document.
	 * @param name name of created node.
	 * @return XML representation of object.
	 */
	public Node toXML(Document document, String name);
	
	/**
	 * Loads object from its XML representation.
	 * @param node XML representation of object. 
	 */
	public void loadFromXML(Node node) throws XMLException;
}
