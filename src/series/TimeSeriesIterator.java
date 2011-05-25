//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package series;

/**
 * Iterator over points of time series. To generate one use @link {@link TimeSeries#getIterator()}.
 * 
 * Note that iterator starts <b>before</b> first point in the series, i.e. {@link #next()} returns the first point.
 * 
 * @author Tomáš Vejpustek
 */
public interface TimeSeriesIterator {

	/**
	 * Resets iterator to the beginning of the time series, i.e. <b>before</b> the first point in the series.
	 */
	public void reset();

	/**
	 * @return <code>true</code> if time series has more elements.
	 */
	public boolean hasNext();

	/**
	 * @return Next point in time series or <code>null</code> if there is none.
	 */
	public TimeSeriesPoint next();

	/**
	 * @return Current point in time series.
	 */
	public TimeSeriesPoint getPoint();

	/**
	 * @return Time of current point in time series.
	 */
	public double getTime();

}