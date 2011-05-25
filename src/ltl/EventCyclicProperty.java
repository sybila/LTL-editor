//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

/**
 * Property which specifies a circle sector (derivative) relating to an Event. It has constant reference point. 
 * @author Tomáš Vejpustek
 *
 */
public class EventCyclicProperty extends CyclicProperty {
	private double reference;
	
	/**
	 * Initializes reference point to zero.
	 */
	public EventCyclicProperty() {
		reference = 0;
	}
	
	/**
	 * Initializes reference point.
	 * @param reference value of reference point.
	 */
	public EventCyclicProperty(double reference) {
		this.reference = reference;
	}

	@Override
	public CyclicProperty clone() {
		CyclicProperty clone = new EventCyclicProperty(reference);
		for (Bound b : Bound.values()) {
			if (hasBound(b)) {
				clone.setBound(b, getBound(b));
			}
		}
		return clone;
	}

	@Override
	public double getCenter() {
		return reference;
	}

}
