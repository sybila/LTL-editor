//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.
package series;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import xml.TimeSeriesSource;

/**
 * Defines transformation of parameters (read from a file) to according implementation of {@link TimeSeriesLoader}.
 * 
 * @author Tomáš Vejpustek
 *
 */
public abstract class AbstractTSLoader implements TimeSeriesLoader {
	
	/**
	 * 
	 * @param src Set of parameters.
	 * @return {@link TimeSeriesLoader} implementation corresponding to input parameters.
	 */
	public static TimeSeriesLoader getLoader(TimeSeriesSource src) throws TSLoaderException, FileNotFoundException { //TODO has to throw an exception
		FileInputStream in  = new FileInputStream(src.getSourceFile());
		if (src.getLoaderName().equals(FieldTSLoader.NAME)) {
			return getFieldLoader(new BufferedReader(new InputStreamReader(in)), src);
		}
		return null;
	}
	
	private static TimeSeriesLoader getFieldLoader(BufferedReader in, TimeSeriesSource src) throws TSLoaderException {
		int time = 0;
		int conc = 0;
		int deriv = 0;
		try {
			time = Integer.valueOf(src.getParameter(FieldTSLoader.P_TIME_INDEX));
			conc = Integer.valueOf(src.getParameter(FieldTSLoader.P_CONC_INDEX));
			deriv = Integer.valueOf(src.getParameter(FieldTSLoader.P_DERIV_INDEX));
		} catch (NumberFormatException nfe) {
			//TODO throw exception
			nfe.printStackTrace();
		}
		if (time < 0 || conc < 0 || deriv < 0) {
			//TODO throw exception
		}
		
		return new FieldTSLoader(in, src.getParameter(FieldTSLoader.P_SEPARATOR), time, conc, deriv);
	}
}
