//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package series;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Signals wrong format of input when loading time series via an implementation of {@link TimeSeriesLoader}.
 * 
 * 
 * @author Tomáš Vejpustek
 *
 */
@SuppressWarnings("serial")
public class TSLoaderFormatException extends TSLoaderException {
	private int lineNum;
	private String lineString;
	
	/**
	 * Specifies error type, message and contents of line on which the error occurred (with its number). 
	 * 
	 * @param type Error type.
	 * @param message Short descriptive message.
	 * @param lineNum Number of line containing error.
	 * @param lineString Contents of line containing error.
	 */
	public TSLoaderFormatException(String type, String message, int lineNum, String lineString) {
		super(type, message);
		this.lineNum = lineNum;
		this.lineString = lineString;
	}

	@Override
	public String getLocalizedMessage() {
		ResourceBundle errors = ResourceBundle.getBundle("series.errors");
		return errors.getString(getType()) + "\n" + MessageFormat.format(errors.getString("tsl_line"), lineNum, lineString);
	}

}
