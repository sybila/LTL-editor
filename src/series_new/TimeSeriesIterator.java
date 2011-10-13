//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package series_new;


/**
 * Iterator over points of time series. To generate one use @link {@link TimeSeries#getIterator()}.
 * 
 * Note that iterator starts <b>before</b> first point in the series, i.e. {@link #next()} returns the first point.
 * 
 * @author Tomáš Vejpustek
 */
public interface TimeSeriesIterator {

	/**
	 * @return <code>true</code> if time series has more elements.
	 */
	public boolean hasNext();

	/**
	 * @return Next point in time series or <code>null</code> if there is none.
	 */
	public TimeSeriesPoint next();
}