//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

import java.awt.geom.Point2D;

import ltl.Property.Bound;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ui.Canvas;
import xml.XMLRepresentable;
import coordinates.Transformation;
import exceptions.XMLException;

/**
 * A graphic primitive signifying transition between two {@link Event}. Adjacent events are stored by each transition.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class Transition implements XMLRepresentable, LTLRepresentable {
	private Event left, right;
	private Property conc, deriv;
	
	/**
	 * Initiates adjacent events.
	 * @param left event to the left of created transition.
	 * @param right event to the right of created transition.
	 */
	public Transition(Event left, Event right) {
		setLeft(left);
		setRight(right);
		conc = new TransitionPositiveProperty(this);
		deriv = new TransitionCyclicProperty(this);
	}
	
	/**
	 * Used with "default" transitions to discern whether they should be saved.
	 * 
	 * @return <code>true</code> if this <code>Transition</code> has no properties assigned.
	 */
	public boolean isEmpty() {
		if (getConcentration().isSet()) {
			return false;
		}
		if (getDerivative().isSet()) {
			return false;
		}
		return true;
	}
	
	/**
	 * @return Event adjacent to this transition from left.
	 */
	public Event getLeft() {
		return left;
	}
	
	/**
	 * @return Event adjacent to this transition from right.
	 */
	public Event getRight() {
		return right;
	}

	/**
	 * Changes event adjacent to this transition from left.
	 * @param left new left adjacent event.
	 */
	public void setLeft(Event left) {
		this.left = left;
	}

	/**
	 * Changes event adjacent to this transition from right.
	 * @param left new right adjacent event.
	 */
	public void setRight(Event right) {
		this.right = right;
	}

	public Property getConcentration() {
		return conc;
	}
	
	public Property getDerivative() {
		return deriv;
	}
	
	/**
	 * Splits this <code>Transition</code> into two with <code>middle</code> in the middle.
	 *  
	 * @return Two new transitions as split by the event. Index of the left one is zero.
	 */
	public Transition[] split(Event middle) {
		if (middle == null) {
			throw new IllegalArgumentException("Cannot split a transition by a null event.");
		}
		Transition[] result = new Transition[2];
		result[0] = clone();
		result[1] = clone();
		
		result[0].right = middle;
		result[1].left = middle;
		return result;
	}
	
	/**
	 * Joins <code>right</code> with this transition. Note, <code>right</code> must be directly to the right of
	 * this transition, i.e. <code>this.getRight() == right.getLeft()</code> must be true.
	 * @return Joined {@link Transition}. 
	 */
	public Transition righJoin(Transition right) {
		Transition result = clone();
		result.setRight(right.getRight());
		
		for (Bound b : Bound.values()) {
			if (getConcentration().hasBound(b) && right.getConcentration().hasBound(b)) {
				double thisBound = getConcentration().getBound(b);
				double rightBound = right.getConcentration().getBound(b);
				if ((b.equals(Bound.UPPER) && (rightBound > thisBound)) || (b.equals(Bound.LOWER) && (rightBound < thisBound))) {
					result.getConcentration().setBound(b, rightBound);
				}
			} else if (right.getConcentration().hasBound(b)) {
				result.getConcentration().setBound(b, right.getConcentration().getBound(b));
			}
			if (getDerivative().hasBound(b) && right.getDerivative().hasBound(b)) {
				double thisBound = getDerivative().getBound(b);
				double rightBound = right.getDerivative().getBound(b);
				if ((b.equals(Bound.UPPER) && (rightBound > thisBound)) || (b.equals(Bound.LOWER) && (rightBound < thisBound))) {
					result.getDerivative().setBound(b, rightBound);
				}
			} else if (right.getDerivative().hasBound(b)) {
				result.getDerivative().setBound(b, right.getDerivative().getBound(b));
			}
		}
		
		return result;
	}

	/**
	 * Tests point on being contained by this transition.
	 * @param p point in on-screen coordinates.
	 * @param coord coordinate transformation.
	 * @return <code>true</code> if this transition contains given point, <code>false</code> otherwise.
	 */
	public boolean contains(Point2D p, Transformation coord) {
		if ((getLeft() != null) && (getLeft().getTime().getCenter() > coord.getTime(p.getX()))) {
			return false;
		}
		if ((getRight() != null) && (getRight().getTime().getCenter() < coord.getTime(p.getX()))) {
			return false;
		}
		
		if (getConcentration().isSet()
				&& (!getConcentration().hasBound(Bound.UPPER) || (getConcentration().getBound(Bound.UPPER) >= coord.getConcentration(p.getY())))
				&& (!getConcentration().hasBound(Bound.LOWER) || (getConcentration().getBound(Bound.LOWER) <= coord.getConcentration(p.getY())))) {
			return true;
		}
		
		//see notes and picture from 04/16
		double leftX = (getLeft() != null) ? coord.getX(getLeft().getTime().getCenter()) : 0;
		double leftY = (getLeft() != null) ? coord.getY(getLeft().getConcentration().getCenter()) : coord.getSize().getY()/2;
		double rightX = (getRight() != null) ? coord.getX(getRight().getTime().getCenter()) : coord.getSize().getX();
		double rightY = (getRight() != null) ? coord.getY(getRight().getConcentration().getCenter()) : coord.getSize().getY()/2;
		double k = (rightY-leftY)/(rightX-leftX);
		double intEnd = (Canvas.INT_END*Math.sqrt(k*k+1));
		double pYOnTrans = k*(p.getX() - leftX) + leftY;
		if (Math.abs(pYOnTrans-p.getY()) <= intEnd) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public Transition clone() {
		Transition result = new Transition(getLeft(), getRight());
		for (Bound b : Bound.values()) {
			if (getConcentration().hasBound(b)) {
				result.getConcentration().setBound(b, getConcentration().getBound(b));
			}
			if (getDerivative().hasBound(b)) {
				result.getDerivative().setBound(b, getDerivative().getBound(b));
			}
		}
		return result;
	}

	@Override
	public void loadFromXML(Node node) throws XMLException {
		NodeList nodes = node.getChildNodes();
		for (int index = 0; index < nodes.getLength(); index++) {
			Node n = nodes.item(index);
			String name = n.getNodeName();
			if (name.equals("concentration")) {
				getConcentration().loadFromXML(n);
			} else if (name.equals("derivative")) {
				getDerivative().loadFromXML(n);
			}
		}
		
	}

	@Override
	public Node toXML(Document document, String name) {
		Element transition =  document.createElement(name);
		transition.appendChild(getConcentration().toXML(document, "concentration"));
		transition.appendChild(getDerivative().toXML(document, "derivative"));
		return transition;
	}

	@Override
	public Node toXML(Document document) {
		return toXML(document, "transition");
	}

	@Override
	public String toLTL(FormulaBuilder builder) {
		if (getConcentration().isSet() && getDerivative().isSet()) {
			return builder.and(builder.concentration(this), builder.derivative(this));
		} else if (getConcentration().isSet()) {
			return builder.concentration(this);
		} else if (getDerivative().isSet()) {
			return builder.derivative(this);
		} else {
			return null;
		}
	}
	
}
