//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package series_new;

/**
 * Point of a time series. Consists of time, a species concentration and its derivative (wrt time) values.
 * 
 * Two <code>TimeSeriesPoint</code> are equal if their time values are equal.
 * 
 * <p>Developer note: Due to <code>Double</code> imprecision, they should always be stored in a <code>java.util.List</code>.
 * In case storage in <code>Set</code> or <code>SortedSet</code> were implemented, consider using <code>BigDecimal</code> for time value.</p>
 *  
 * @author Tomáš Vejpustek
 * 
 */
// Container for three doubles -- not supposed to change after creation.
//
public class TimeSeriesPoint {
	private double time, concentration, derivative;
	
	/**
	 * Specifies all values.
	 * @param time Time value of point
	 * @param concentration Species concentration
	 * @param derivative Species concentration derivative
	 * @throws IllegalArgumentException when time or concentration values are negative.
	 */
	TimeSeriesPoint (double time, double concentration, double derivative) {
		if (time < 0) {
			throw new IllegalArgumentException("Time value of a TimeSeriesPoint cannot be negative.");
		}
		if (concentration < 0) {
			throw new IllegalArgumentException("Concentration value of a TimeSeriesPoint cannot be negative.");
		}
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
	
	/**
	 * @return Time value.
	 */
	public double getTime() {
		return time;
	}

	//generated automatically
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(time);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	//generated automatically
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TimeSeriesPoint)) {
			return false;
		}
		TimeSeriesPoint other = (TimeSeriesPoint) obj;
		if (Double.doubleToLongBits(time) != Double
				.doubleToLongBits(other.time)) {
			return false;
		}
		return true;
	}
	
}