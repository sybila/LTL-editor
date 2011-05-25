//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

/**
 * Property specifying a circle sector (derivative) related to {@link Transition}.
 * Its reference point is relative to adjacent {@link Event}s. Therefore, parent {@link Transition} must be specified.
 * @author Tomáš Vejpustek
 *
 */
public class TransitionCyclicProperty extends CyclicProperty {
	private static double concEnd, timeEnd;
	private Transition parent;
	
	/**
	 * Initializes parent {@link Transition}.
	 */
	public TransitionCyclicProperty(Transition parent) {
		this.parent = parent;	
	}

	@Override
	public CyclicProperty clone() {
		CyclicProperty clone = new TransitionCyclicProperty(parent);
		for (Bound b : Bound.values()) {
			if (hasBound(b)) {
				clone.setBound(b, getBound(b));
			}
		}
		return clone;
	}

	@Override
	public double getCenter() {
		double leftT = (parent.getLeft() != null) ? parent.getLeft().getTime().getCenter() : 0;
		double leftC = (parent.getLeft() != null) ? parent.getLeft().getConcentration().getCenter() : timeEnd;
		double rightT = (parent.getRight() != null) ? parent.getRight().getTime().getCenter() : concEnd;
		double rightC = (parent.getRight() != null) ? parent.getRight().getConcentration().getCenter() : concEnd;
		return (rightC-leftC)/(rightT-leftT);
	}

	/**
	 * Sets concentration values for the case one of adjacent {@link Event}s is <code>null</code>.
	 */
	public static void setConcentrationEnd(double value) {
		concEnd = value;
	}

	/**
	 * Sets maximum time of time series for the case right adjacent {@link Event} is <code>null</code>. 
	 */
	public static void setTimeEnd(double value) {
		timeEnd = value;
	}
	
	

}
