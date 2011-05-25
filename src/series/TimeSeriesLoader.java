//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package series;

/**
 * Wrapper for time series input (e.g. a StringReader). Reads the time series sequentially from t=0.
 * 
 * @author Tomáš Vejpustek
 *
 */
public interface TimeSeriesLoader {

	/**
	 * @return Time interval between two consecutive time series points.
	 */
	public double getInterval();
	
	/**
	 * Reads a single point. Used to read the points sequentially.
	 * 
	 * @return Point read or <code>null</code> if the end has been reached. 
	 * @throws TSLoaderException when IO error is encountered.
	 */
	public TimeSeriesPoint readPoint() throws TSLoaderException;
}
