//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package exceptions;


/**
 * Exception thrown as a result of error during XML parsing. 
 * 
 * @author Tomáš Vejpustek
 *
 */
@SuppressWarnings("serial")
public class XMLException extends LocalizedException {
	
	public XMLException(String type, String message) {
		super(type, message);
	}
	
	public XMLException(String type, String message, Throwable cause) {
		super(type, message, cause);
	}
}
