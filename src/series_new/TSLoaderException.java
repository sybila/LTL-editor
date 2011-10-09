//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package series_new;

import java.util.ResourceBundle;

/**
 * 
 * Signals some sort of error while loading Time Series via an implementation of {@link TimeSeriesLoader}.
 * 
 * @author Tomáš Vejpustek
 *
 */
@SuppressWarnings("serial")
public class TSLoaderException extends Exception {
	private String type;
	private Throwable cause;

	/**
	 * Specifies error type (used by method {@link TSLoaderException#getLocalizedMessage} to access ResourceBundle)
	 * and message. 
	 * 
	 * @param type Type of error
	 * @param message Short descriptive message
	 */
	public TSLoaderException(String type, String message) {
		super(message);
		this.type = type;
		cause = null;
	}
	
	/**
	 * Specifies error type (used by method {@link TSLoaderException#getLocalizedMessage} to access ResourceBundle),
	 * message and its cause.
	 * 
	 * @param type Type of error
	 * @param message Short descriptive message
	 * @param cause An exception causing this type of error
	 */
	public TSLoaderException(String type, String message, Throwable cause) {
		super(message);
		this.type = type;
		this.cause = cause;
	}
	
	/**
	 * @return Type of exception.
	 */
	protected String getType() {
		return type;
	}

	@Override
	public String getLocalizedMessage() {
		ResourceBundle errors = ResourceBundle.getBundle("series_new.messages");
		StringBuilder out = new StringBuilder(errors.getString(type));
		if (cause != null) {
			out.append("\n");
			out.append(cause.getLocalizedMessage());
		}
		return out.toString();
	}
	
}