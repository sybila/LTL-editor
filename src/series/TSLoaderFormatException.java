//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package series;

import java.text.MessageFormat;

/**
 * Signals wrong format of input when loading time series via an implementation of {@link TimeSeriesLoader}.
 * In addition to error description (as of {@link TSLoaderException}) prints the line where error was encountered and its number.
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
		StringBuilder out = new StringBuilder(super.getLocalizedMessage());
		out.append('\n');
		out.append(MessageFormat.format(getBundle().getString("tsl_line"), lineNum, lineString));
		return out.toString();
	}

}
