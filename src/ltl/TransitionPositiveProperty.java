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
 * Property which has no fixed reference point and instead relies on other two other property references,
 * specifically concentrations of two {@link Event}s adjacent to a {@link Transition}.  
 * 
 * Cannot be in point state and does not support operation {@link #makePoint()} and {@link #move(double)}.
 * 
 * Used to denote concentration of {@link Transition}.
 * 
 * @author Tomáš Vejpustek
 */
public class TransitionPositiveProperty implements Property {
	private static double end = 0;
	
	private Map<Bound, Double> bounds = new HashMap<Bound, Double>();
	private Transition parent;
	
	/**
	 * Initializes contained {@link Transition}. 
	 */
	public TransitionPositiveProperty(Transition parent) {
		this.parent = parent;
	}
	
	/**
	 * Checks whether the change (as in {@link #setBound(ltl.Property.Bound, double)}) makes
	 * one bound exceed the other and in that case unsets the property.
	 * @param bound Bound to be changed.
	 * @param value New value of <code>bound</code>.
	 * @return <code>true</code> when the property was unset, <code>false</code> otherwise.
	 */
	protected boolean checkExceeding(Bound bound, double value) {
		if (hasBound(bound.other())) {
			double other = bounds.get(bound.other());
			if ((bound.equals(Bound.UPPER) && value < other) || (bound.equals(Bound.LOWER) && value > other)) {
				unset();
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
		if (!hasBound(bound)) {
			if (bound.equals(Bound.UPPER)) {
				return Double.POSITIVE_INFINITY;
			} else {
				return 0;
			}
		}
		return bounds.get(bound);
	}

	@Override
	public double getCenter() {
		double left = (parent.getLeft() != null) ? parent.getLeft().getConcentration().getCenter() : end;
		double right = (parent.getRight() != null) ? parent.getRight().getConcentration().getCenter() : end;
		return (left+right)/2;
	}

	@Override
	public boolean hasBound(Bound bound) {
		return (bounds.get(bound) != null);
	}

	@Override
	public boolean hasBounds() {
		return (hasBound(Bound.UPPER) && hasBound(Bound.LOWER));
	}

	@Override
	public boolean isPoint() {
		return false;
	}

	@Override
	public boolean isSet() {
		return (hasBound(Bound.UPPER) || hasBound(Bound.LOWER));
	}

	@Override
	public void makePoint() {
		throw new UnsupportedOperationException("Cannot make point from a TransitionPositiveProperty.");
	}

	@Override
	public void move(double value) {
		throw new UnsupportedOperationException("Cannot move a TransitionPositiveProperty.");
	}

	@Override
	public void refreshReference() {
		//does nothing
	}

	@Override
	public void setBound(Bound bound, double value) {
		stretchBound(bound, value);
	}

	@Override
	public void stretchBound(Bound bound, double value) {
		if (!checkExceeding(bound, value)) {
			if (value <= 0) {
				throw new IllegalArgumentException("Positive property cannot have value lesser or equal to zero.");
			}
			bounds.put(bound, value);
		}
	}

	@Override
	public void unbind(Bound bound) {
		if (hasBound(bound.other())) {
			bounds.put(bound, null);
		} else {
			unset();
		}
	}

	@Override
	public void unset() {
		bounds.clear();
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
				double upper = Double.valueOf(n.getChildNodes().item(0).getNodeValue());
				if (upper <= 0) {
					throw new XMLException("err_xml_positive", "Non-positive value of upper bound.");
				}
				bounds.put(Bound.UPPER, upper);
				hasUpper = true;
			} else if (name.equals("lower")) {
				double lower = Double.valueOf(n.getChildNodes().item(0).getNodeValue());
				if (lower <= 0) {
					throw new XMLException("err_xml_positive", "Non-positive value of lower bound.");
				}
				bounds.put(Bound.LOWER, lower);
				hasLower = true;
			}
		}
		if (!hasLower) {
			unbind(Bound.LOWER);
		}
		if (!hasUpper) {
			unbind(Bound.UPPER);
		}
		if (hasLower && hasUpper && bounds.get(Bound.LOWER) > bounds.get(Bound.UPPER)){
			throw new XMLException("err_xml_bounds_order", "Lower bound exceeds the upper one.");
		}
		
	}

	@Override
	public Node toXML(Document document) {
		return toXML(document, "relative_property");
	}

	@Override
	public Node toXML(Document document, String name) {
		Element property = document.createElement(name);
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
	public Property clone() {
		TransitionPositiveProperty clone = new TransitionPositiveProperty(parent);
		clone.bounds = new HashMap<Bound, Double>(bounds);
		return clone;
	}

	/**
	 * Sets values of end points in case parent {@link Transition} has one of the adjacent {@link Event} <code>null</code>.
	 */
	public static void setEnd(double value) {
		if (value < 0) {
			throw new IllegalArgumentException("Positive coordinate cannot have negative value.");
		}
		end = value;
	}
}
