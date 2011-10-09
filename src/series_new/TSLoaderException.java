//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package series_new;

import exceptions.LocalizedException;

/**
 * 
 * Signals some sort of error while loading Time Series via an implementation of {@link TimeSeriesLoader}.
 * 
 * @author Tomáš Vejpustek
 *
 */
@SuppressWarnings("serial")
public class TSLoaderException extends LocalizedException {
	public TSLoaderException(String type, String message) {
		super(type,message);
	}
	
	public TSLoaderException(String type, String message, Throwable cause) {
		super(type, message, cause);
	}
}