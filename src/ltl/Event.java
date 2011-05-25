//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.Queue;

import ltl.Property.Bound;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ui.Canvas;
import coordinates.Transformation;

/**
 * A graphic primitive signifying a point in the course of time series.
 * 
 *  Consists of three properties: time, species concentration and its derivative.
 *  Time and derivation are mandatory in the sense that they specify a reference
 *  point on time series. 
 * 
 * @author Tomáš Vejpustek
 *
 */
public class Event implements Cloneable, XMLRepresentable, LTLRepresentable {
	private Property time, concentration;
	private Property derivative = new EventCyclicProperty();
	
	/**
	 * Empty selector for the purpose of loading from XML.
	 */
	private Event() {}
	
	/**
	 * Creates event with specified reference point. 
	 * @param time time value of reference point.
	 * @param concentration concentration value of reference point.
	 */
	public Event(double time, double concentration) {
		this.time = new PositiveProperty(time);
		this.concentration = new PositiveProperty(concentration);
	}
	
	/**
	 * @return Time property.
	 */
	public Property getTime() {
		return time;
	}
	
	/**
	 * @return Species concentration property.
	 */
	public Property getConcentration() {
		return concentration;
	}

	/**
	 * @return Species concentration derivative property.
	 */
	public Property getDerivative() {
		return derivative;
	}
	
	/**
	 * Tests a point on being contained by this Event.
	 * @param p Point in on-screen coordinates.
	 * @param coord Coordinate transformation between on-screen and model coordinates.
	 * @return <code>true</code> if this event contains given point, <code>false</code> otherwise.
	 */
	public boolean contains(Point2D p, Transformation coord) {
		double x1, y1, x2, y2; //bounding rectangle specification
		
		if (getTime().isPoint() || ! getTime().isSet()) {
			double refX = coord.getX(getTime().getCenter());
			x1 = refX - Canvas.INT_END;
			x2 = refX + Canvas.INT_END;
		} else {
			if (getTime().hasBound(Bound.LOWER)) {
				x1 = coord.getX(getTime().getBound(Bound.LOWER));
			} else {
				x1 = 0;
			}
			if (getTime().hasBound(Bound.UPPER)) {
				x2 = coord.getX(getTime().getBound(Bound.UPPER));
			} else {
				x2 = coord.getSize().getX();
			}
		}
		
		if (getConcentration().isPoint() || !getConcentration().isSet()) {
			double refY = coord.getY(getConcentration().getCenter());
			y1 = refY - Canvas.INT_END;
			y2 = refY + Canvas.INT_END;
		} else {
			if (getConcentration().hasBound(Bound.LOWER)) {
				y2 = coord.getY(getConcentration().getBound(Bound.LOWER));
			} else {
				y2 = coord.getSize().getY();
			}
			if (getConcentration().hasBound(Bound.UPPER)) {
				y1 = coord.getY(getConcentration().getBound(Bound.UPPER));
			} else {
				y1 = 0;
			}
		}
		
		return (new Rectangle2D.Double(x1, y1, x2-x1, y2-y1)).contains(p);
	}
	
	/**
	 * Events on a time series may overlap and may cover various area. Generally, the smaller the value,
	 * the more likely this event will be selected among overlapping ones.
	 * 
	 *  Note, the following values are only guidelines. <b>Do not</b> use this method to discern the type of this event.
	 * @return <ul>
	 * 	<li><code>0</code> -- single point</li>
	 *	<li><code>1</code> -- interval in concentration</li>
	 *	<li><code>2</code> -- interval in time</li>
	 *	<li><code>3</code> -- interval in both time and concentration</li>
	 *	<li><code>4</code> -- unbound interval</li> 
	 * @see Event#contains(Point2D, Transformation)
	 */
	public int selectionPriority() {
		if (getTime().isPoint() || !getTime().isSet()) {
			if (getConcentration().isPoint() || !getConcentration().isSet()) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (getConcentration().isPoint() || !getConcentration().isSet()) {
				return 2;
			} else {
				if (getTime().hasBounds() && getConcentration().hasBounds()) {
					return 3;
				} else {
					return 4;
				}
			}
		}
	}

	/**
	 * @return <code>true</code> if this event has no set properties, <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		return (!getTime().isSet() && !getConcentration().isSet() && !getDerivative().isSet());
	}
	
	@Override
	public Event clone() {
		Event clone = new Event();
		clone.time = time.clone();
		clone.concentration = concentration.clone();
		clone.derivative = derivative.clone();
		return clone;
	}
	
	@Override
	public Node toXML(Document document) {
		return toXML(document, "event");
	}
	
	@Override
	public Node toXML(Document document, String name) {
		Element event = document.createElement(name);
		event.appendChild(getTime().toXML(document, "time"));
		event.appendChild(getConcentration().toXML(document, "concentration"));
		event.appendChild(getDerivative().toXML(document, "derivative"));
		return event;
	}
	
	@Override
	public void loadFromXML(Node node) throws XMLException{
		NodeList nodes = node.getChildNodes();
		for (int index = 0; index < nodes.getLength(); index++) {
			Node n = nodes.item(index);
			String name = n.getNodeName();
			if (name.equals("time")) {
				getTime().loadFromXML(n);
			} else if (name.equals("concentration")) {
				getConcentration().loadFromXML(n);
			} else if (name.equals("derivative")) {
				getDerivative().loadFromXML(n);
			}
		}
	}
	
	@Override
	public String toString() {
		return ("[" + getTime() + ", " + getConcentration() + "]"); 
	}

	@Override
	public String toLTL(FormulaBuilder builder) {
		Queue<String> properties = new LinkedList<String>();
		if (getTime().isSet()) {
			properties.add(builder.time(this));
		}
		if (getConcentration().isSet()) {
			properties.add(builder.concentration(this));
		}
		if (getDerivative().isSet()) {
			properties.add(builder.derivative(this));
		}
		
		if (properties.isEmpty()) {
			return null;
		} else {
			String formula = properties.remove();
			while (!properties.isEmpty()) {
				formula = builder.and(formula, properties.remove());
			}
			return formula;
		}
	}
	
}
