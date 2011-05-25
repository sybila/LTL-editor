//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

import java.util.ResourceBundle;

/**
 * Fully localized exception thrown as a result of error during XML parsing.
 * Localized messages are stored in a resource bundle 
 * 
 * @author Tomáš Vejpustek
 *
 */
@SuppressWarnings("serial")
public class XMLException extends Exception {
	private String type;
	private Throwable cause;

	/**
	 * Specifies type of the error (used to access resource bundle).
	 * @param type Type of error.
	 * @param message Internal error message.
	 */
	public XMLException(String type, String message) {
		super(message);
		this.type = type;
		cause = null;
	}
	
	/**
	 * Specifies type of the error and its cause. 
	 * @param type Type of error.
	 * @param message Internal error message.
	 * @param cause An exception causing this type of error.
	 */
	public XMLException(String type, String message, Throwable cause) {
		super(message);
		this.type = type;
		this.cause = cause;
	}
	
	/**
	 * @return Type of this exception specified during its creation.
	 */
	protected String getType() {
		return type;
	}
	
	@Override
	public String getLocalizedMessage() {
		ResourceBundle errors = ResourceBundle.getBundle("ltl.errors");
		StringBuilder out = new StringBuilder(errors.getString(type));
		if (cause != null) {
			out.append("\n");
			out.append(cause.getLocalizedMessage());
		}
		return out.toString();
	}
}
