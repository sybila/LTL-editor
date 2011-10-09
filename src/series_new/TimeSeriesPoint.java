//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package series_new;

/**
 * Point of a time series. Consists of time, a species concentration and its derivative with respect to time.
 * 
 * Two TimeSeriesPoint are equal if their time values are equal.
 *  
 * @author Tomáš Vejpustek
 * 
 */
// Container for three doubles -- not supposed to change after creation.
public class TimeSeriesPoint {
	private double time, concentration, derivative;
	
	/**
	 * @param time Time value of point
	 * @param concentration Species concentration
	 * @param derivative Species concentration derivative
	 */
	TimeSeriesPoint (double time, double concentration, double derivative) {
		this.time = time;
		this.concentration = concentration;
		this.derivative = derivative;
	}

	/**
	 * @return Species concentration in given point
	 */
	public double getConcentration() {
		return concentration;
	}

	/**
	 * @return Species concentration derivative in given point
	 */
	public double getDerivative() {
		return derivative;
	}	
	
}
