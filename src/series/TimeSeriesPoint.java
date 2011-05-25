//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package series;

/**
 * Consists of a species concentration and its derivative with respect to time.
 *  
 * @author Tomáš Vejpustek
 * 
 */
// Container for two doubles -- not supposed to change after creation.
public class TimeSeriesPoint {
	private double concentration, derivative;
	
	/**
	 * 
	 * @param concentration Species concentration
	 * @param derivative Species concentration derivative
	 */
	TimeSeriesPoint(double concentration, double derivative) {
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

	//Generated automatically
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(concentration);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(derivative);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TimeSeriesPoint))
			return false;
		TimeSeriesPoint other = (TimeSeriesPoint) obj;
		if (Double.doubleToLongBits(concentration) != Double
				.doubleToLongBits(other.concentration))
			return false;
		if (Double.doubleToLongBits(derivative) != Double
				.doubleToLongBits(other.derivative))
			return false;
		return true;
	}
	
	
}
