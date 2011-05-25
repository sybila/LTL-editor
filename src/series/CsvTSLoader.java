//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package series;

import java.io.BufferedReader;

/**
 * Reads time series from a CSV file. Note that period is used as decimal separator locale notwithstanding.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class CsvTSLoader extends AbstractLineTSLoader {
	
	/**
	 * @param input Input Reader.
	 * @throws TSLoaderException when input error or wrong input format encountered.
	 */
	public CsvTSLoader(BufferedReader input) throws TSLoaderException{
		super(input);
	}

	@Override
	protected void checkFormat(String line) throws TSLoaderFormatException {
		String [] fields = splitLine(line);
		if (fields.length != 3) {
			throw new TSLoaderFormatException("tsl_field_number", "Wrong number of fields", getLineNum(), line);
		}
	}

	@Override
	protected double getConcentration(String line) throws TSLoaderFormatException {
		return getField(line, 1);
	}

	@Override
	protected double getDerivative(String line) throws TSLoaderFormatException {
		return getField(line, 2);
	}

	@Override
	protected double getTime(String line) throws TSLoaderFormatException {
		return getField(line, 0);
	}

	@Override
	protected void initialize() throws TSLoaderException {
		// do nothing
	}

	/**
	 * Splits input line to fields
	 * 
	 * @param line Input line
	 * @return Array of fields
	 */
	private String [] splitLine(String line) {
		return line.split(",");
	}
	
	/**
	 *	Recovers particular field from input line.
	 * 
	 * @param line One line of input.
	 * @param column Number of field.
	 */
	private double getField(String line, int column) throws TSLoaderFormatException {
		String [] fields = splitLine(line);
		double value;
		try {
			value = Double.valueOf(fields[column]);
		} catch (NumberFormatException nfe) {
			throw new TSLoaderFormatException("tsl_number_format", "Illegible decimal number.", getLineNum(), line);
		}
		return value;
	}
}
