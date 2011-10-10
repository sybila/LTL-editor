//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xml.XMLException;

/**
 * Property which specifies a circle sector (or rather an angle -- via its <code>tan</code> value)
 * and so lower bound may exceed upper.
 * 
 * Note: Always bound -- unbound would mean positive or negative infinity.
 * 
 * Used to indicate species derivative.
 * 
 * @author Tomáš Vejpustek
 *
 */
public abstract class CyclicProperty implements Property {
	private Map<Bound, Double> values = new HashMap<Bound, Double>(); //note when LOWER bound is not set, it is point, otherwise, it is unset
	

	@Override
	public double getBound(Bound bound) {
		if (hasBound(bound)) {
			return values.get(bound);
		} else {
			return Double.NaN;
		}
	}

	@Override
	public boolean hasBound(Bound bound) {
		return (values.get(bound) != null);
	}

	@Override
	public boolean hasBounds() {
		return (hasBound(Bound.UPPER) && hasBound(Bound.LOWER));
	}

	@Override
	public boolean isPoint() {
		return (hasBound(Bound.UPPER) && !hasBound(Bound.LOWER));
	}

	@Override
	public boolean isSet() {
		return !values.isEmpty();
	}

	@Override
	public void makePoint() {
		if (!hasBound(Bound.UPPER)) {
			if (!hasBound(Bound.LOWER)) {
				values.put(Bound.UPPER, getCenter());
			} else {
				values.put(Bound.UPPER, values.get(Bound.LOWER));
			}
		}
		values.put(Bound.LOWER, null);
	}

	@Override
	public void move(double value) {
		throw new UnsupportedOperationException("Cannot move cyclic property.");
	}

	@Override
	public void refreshReference() {
		throw new UnsupportedOperationException("Cannot refresh reference of cyclic property.");

	}

	@Override
	public void setBound(Bound bound, double value) {
		stretchBound(bound, value);
	}

	@Override
	public void stretchBound(Bound bound, double value) {
		values.put(bound, value);
	}

	@Override
	public void unbind(Bound bound) {
		values.put(bound, null);
	}

	@Override
	public void unset() {
		values.clear();
	}

	@Override
	public void loadFromXML(Node node) throws XMLException {
		NodeList nodes = node.getChildNodes();
		boolean hasUpper = false;
		boolean hasLower = false;
		for (int index = 0; index < nodes.getLength(); index++) {
			Node n = nodes.item(index);
			String name = n.getNodeName();
			if (name.equals("upper")) {
				values.put(Bound.UPPER, Double.valueOf(n.getFirstChild().getNodeValue()));
				hasUpper = true;
			} else if (name.equals("lower")) {
				values.put(Bound.LOWER, Double.valueOf(n.getFirstChild().getNodeValue()));
				hasLower = true;
			}
		}
		if (hasLower && !hasUpper) {
			throw new XMLException("err_xml_cyclic", "State when lower bound is set and upper is not cannot occur in cyclic property.");
		}
	}

	@Override
	public Node toXML(Document document) {
		return toXML(document, "cyclic_property");
	}

	@Override
	public Node toXML(Document document, String name) {
		Element property = document.createElement(name);
		if (hasBound(Bound.UPPER)) {
			Element up = document.createElement("upper");
			up.appendChild(document.createTextNode(Double.toString(values.get(Bound.UPPER))));
			property.appendChild(up);
		}
		if (hasBound(Bound.LOWER)) {
			Element low = document.createElement("lower");
			low.appendChild(document.createTextNode(Double.toString(values.get(Bound.LOWER))));
			property.appendChild(low);
		}
		return property;
	}
	
	@Override
	public abstract CyclicProperty clone();
	
}
