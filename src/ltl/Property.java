//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

/**
 * Abstract property interval (such as time or species concentration). Defined by two endpoints, or <b>bounds</b>.
 * 
 * Has a reference point, which is always set (see {@link #getCenter()}).
 * 
 * Has three states: interval, point and not being set. 
 * 
 * @author Tomáš Vejpustek
 *
 */
public interface Property extends XMLRepresentable{
	/**
	 * Defines two end points of an interval.
	 * 
	 * @author Tomáš Vejpustek
	 */
	public static enum Bound {
		UPPER,
		LOWER;
		
		/**
		 * @return The other end point, i.e. <code>UPPER</code> for <code>LOWER</code> and vice versa.
		 */
		public Bound other() {
			if (this.equals(UPPER)) {
				return LOWER;
			} else {
				return UPPER;
			}
		}
	}
	
	/**
	 * @return The reference point. Always set.
	 */
	public double getCenter();
	
	/** 
	 * @return Value of given <code>bound</code>. Behavior may not be defined, if it is unbound.
	 */
	public double getBound(Bound bound);
	
	
	/**
	 * @return <code>true</code> when having both lower and upper bound, <code>false</code> otherwise (including not set state)
	 */
	public boolean hasBounds();
	
	/**
	 * @return <code>true</code> when having given <code>bound</code>, <code>false</code> otherwise (including not set state)
	 */
	public boolean hasBound(Bound bound);
	
	/**
	 * @return <code>true</code> when being point, <code>false</code> otherwise
	 */
	public boolean isPoint();
	
	/**
	 * @return <code>false</code> when not set, <code>true</code> otherwise
	 */
	public boolean isSet(); 
	
	
	/**
	 * Sets given <code>bound</code> and changes reference point accordingly. Do not use to make one of the bounds infinite.
	 * 
	 * @param value Desired value of end point.
	 */
	public void setBound(Bound bound, double value);
	
	/**
	 * Sets given <code>bound</code> and does not change reference point. Used by dragging operations.
	 * 
	 * @param value Desired value of end point.
	 */
	public void stretchBound(Bound bound, double value);
	
	/**
	 * Moves reference point to given <code>value</code> and adjusts bounds accordingly. Used to change value of a point.
	 */
	public void move(double value);
	
	/**
	 * Unbinds given <code>bound</code>, i.e. makes end point infinite.
	 * 
	 * Unbiding both end points is equivalent to {@link #unset()} operation.  
	 */
	public void unbind(Bound bound);
	
	/**
	 * Makes a point from property with the value of reference point. 
	 */
	public void makePoint();
	
	/**
	 * Makes the property not set.
	 */
	public void unset();
	
	/**
	 * If the interval is bounded, refreshes reference so that it is in the middle.
	 */
	public void refreshReference();
	
	public Property clone();
}
