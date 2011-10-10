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
 * Positive property with lower bound zero (e.g. time, species concentration).
 * 
 * Setting lower bound to greater than upper or upper bound to smaller than lower is equal to operation
 * {@link #makePoint()} with the reference point being set at the bound unaffected by the operation. 
 * 
 * @author Tomáš Vejpustek
 */
public class PositiveProperty implements Property {
	//bound -- both value specify end points
	//unbound -- one value is null
	//point -- both values are null
	private double reference;
	private Map<Bound, Double> values = new HashMap<Bound, Double>();
	private boolean set;
	
	/**
	 * Creates not set property.
	 * @param value Value of reference point.
	 * @throws IllegalArgumentException when <code>value</code> is not positive.
	 */
	public PositiveProperty(double value) {
		if (value <= 0) {
			throw new IllegalArgumentException("Positive property cannot have value lesser or equal to zero."); 
		}
		set = false;
		reference = value;
	}
	
	/**
	 * Creates interval property. If specified values are in wrong order (i.e. lower is greater than upper),
	 * they are switched. If they are equal (do not use in this way), they are created as points. 
	 *  
	 * @param lower Value of lower bound.
	 * @param upper Value of upper bound.
	 * @throws IllegalArgumentException <code>upper</code> or <code>lower</code> is not positive.
	 */
	public PositiveProperty(double lower, double upper) {
		if ((lower <= 0) || (upper <= 0)) {
			throw new IllegalArgumentException("Positive property cannot have value lesser or equal to zero.");
		}
		set = true;
		reference = (lower+upper)/2;
		if (lower > upper) {
			values.put(Bound.UPPER, lower);
			values.put(Bound.LOWER, upper);
		} else if (lower < upper) {
			values.put(Bound.UPPER, upper);
			values.put(Bound.LOWER, lower);
		}	
	}

	@Override
	public void refreshReference() {
		if (hasBounds()) {
			reference = (getBound(Bound.UPPER)+getBound(Bound.LOWER))/2;
		}
	}
	
	/**
	 * Checks whether the change (as in {@link #setBound(ltl.Property.Bound, double)}) makes
	 * one bound exceed the other and in that case makes the property a point.
	 * @param bound Bound to be changed.
	 * @param value New value of <code>bound</code>.
	 * @return <code>true</code> when the property was changed into point, <code>false</code> otherwise.
	 */
	protected boolean checkExceeding(Bound bound, double value) {
		if (hasBound(bound.other()) || isPoint()) {
			double other = isPoint() ? getCenter() : values.get(bound.other());
			if ((bound.equals(Bound.UPPER) && value < other) || (bound.equals(Bound.LOWER) && value > other)) {
				reference = other;
				makePoint();
				return true;
			} 
		}
		return false;
	}
	
	@Override
	public double getBound(Bound bound) {
		if (!isSet()) {
			return Double.NaN;
		}
		if (isPoint()) {
			return getCenter();
		}
		if (!hasBound(bound)) {
			if (bound.equals(Bound.UPPER)) {
				return Double.POSITIVE_INFINITY;
			} else {
				return 0;
			}
		}
		return values.get(bound);
	}

	@Override
	public double getCenter() {
		return reference;
	}

	@Override
	public boolean hasBound(Bound bound) {
		return (values.get(bound) != null);
	}

	@Override
	public boolean hasBounds() {
		return hasBound(Bound.LOWER) && hasBound(Bound.UPPER);
	}

	@Override
	public boolean isPoint() {
		return (isSet() && !hasBound(Bound.LOWER) && !hasBound(Bound.UPPER));
	}

	@Override
	public boolean isSet() {
		return set;
	}

	@Override
	public void makePoint() {
		set = true;
		values.clear();
	}

	@Override
	public void setBound(Bound bound, double value) {
		stretchBound(bound, value);
		refreshReference();
	}
	
	@Override
	public void stretchBound(Bound bound, double value) {
		if (!isSet()) {
			set = true;
		}
		if (!checkExceeding(bound, value)) {
			if (value <= 0) {
				throw new IllegalArgumentException("Positive property cannot have value lesser or equal to zero.");
			}
			if (isPoint()) {
				values.put(bound.other(), getCenter());
			}
			values.put(bound, value);
		}
	}
	
	//if LOWER bound is lesser than zero, it permits only move to zero
	@Override
	public void move(double value) {
		if (value < 0) {
			throw new IllegalArgumentException("Positive property cannot have value lesser or equal to zero.");
		}
		double d = value-getCenter();
		if (hasBound(Bound.LOWER) && (values.get(Bound.LOWER)+d < 0)) {
			d = -values.get(Bound.LOWER);
		}
		reference = getCenter()+d;
		if (hasBound(Bound.UPPER)) {
			values.put(Bound.UPPER, values.get(Bound.UPPER)+d);
		}
		if (hasBound(Bound.LOWER)) {
			values.put(Bound.LOWER, values.get(Bound.LOWER)+d);
		}
	}

	@Override
	public void unbind(Bound bound) {
		if (hasBound(bound.other())) {
			values.put(bound, null);
		} else {
			unset();
		}
	}

	@Override
	public void unset() {
		set = false;
		values.clear();
	}
	
	@Override
	public Property clone() {
		PositiveProperty clone = new PositiveProperty(reference);
		clone.set = set;
		clone.values = new HashMap<Bound, Double>(values);
		return clone;
	}

	@Override
	public Node toXML(Document document) {
		return toXML(document, "property");
	}
	
	@Override
	public Node toXML(Document document, String name) {
		Element property = document.createElement(name);
		property.setAttribute("set", (isSet() ? "true" : "false"));
		
		Element ref = document.createElement("reference");
		ref.appendChild(document.createTextNode(Double.toString(getCenter())));
		property.appendChild(ref);
		
		if (hasBound(Bound.UPPER)) {
			Element up = document.createElement("upper");
			up.appendChild(document.createTextNode(Double.toString(getBound(Bound.UPPER))));
			property.appendChild(up);
		}
		
		if (hasBound(Bound.LOWER)) {
			Element low = document.createElement("lower");
			low.appendChild(document.createTextNode(Double.toString(getBound(Bound.LOWER))));
			property.appendChild(low);
		}
		return property;
	}

	@Override
	public void loadFromXML(Node node) throws XMLException{
		NodeList nodes = node.getChildNodes();
		set = true;
		boolean hasLower = false;
		boolean hasUppper = false;
		for (int index = 0; index < nodes.getLength(); index++) {
			Node n = nodes.item(index);
			String name = n.getNodeName();
			if (name.equals("reference")) {
				reference = Double.valueOf(n.getFirstChild().getNodeValue());
				if (reference <= 0) {
					throw new XMLException("err_xml_positive", "Non-positive value of reference.");
				}
			} else if (name.equals("lower")) {
				double lower = Double.valueOf(n.getFirstChild().getNodeValue());
				if (lower <= 0) {
					throw new XMLException("err_xml_positive", "Non-positive value of upper bound.");
				}
				values.put(Bound.LOWER, lower);
				hasLower = true;
			} else if (name.equals("upper")) {
				double upper = Double.valueOf(n.getFirstChild().getNodeValue());
				if (upper <= 0) {
					throw new XMLException("err_xml_positive", "Non-positive value of lower bound.");
				}
				values.put(Bound.UPPER, upper);
				hasUppper = true;
			}	
		}
		if (!hasUppper) {
			unbind(Bound.UPPER);
		} else if (values.get(Bound.UPPER) < reference) {
			throw new XMLException("err_xml_bounds_order", "Reference value exceeds upper bound.");
		}
		if (!hasLower) {
			unbind(Bound.LOWER);
		} else if (values.get(Bound.LOWER) > reference) {
			throw new XMLException("err_xml_bounds_order", "Lower bound exceeds reference.");
		}
		if (node.getAttributes().getNamedItem("set").getNodeValue().equals("false")) {
			unset();
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (!isSet()) {
			result.append("(");
		}
		result.append(getCenter());
		if (!isSet()) {
			result.append(")");
		}
		if (hasBound(Bound.UPPER) || hasBound(Bound.LOWER)) {
			result.append(": (");
			if (hasBound(Bound.LOWER)) {
				result.append(getBound(Bound.LOWER));
			}
			result.append(", ");
			if (hasBound(Bound.UPPER)) {
				result.append(getBound(Bound.UPPER));
			}
			result.append(")");
		}
		return result.toString();
	}
}
