//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package series;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Basic operations on time series inputs with time, species concentration and its derivation
 * in given point on each line. Presumes interval consistency.
 * 
 * @author Tomáš Vejpustek
 *
 */
//NOTE: Cannot test interval consistency due to double rounding issues.
public abstract class AbstractLineTSLoader implements TimeSeriesLoader {
	private BufferedReader input;
	private Queue<String> lines = new LinkedList<String>();
	private double interval;
	private int lineNum;
	
	/**
	 * Initializes input and gets time interval. 
	 * 
	 * @param Input Reader
	 * @throws TSLoaderException when input error or wrong format input is encountered.
	 */
	public AbstractLineTSLoader(BufferedReader input) throws TSLoaderException{
		this.input = input;
		initialize();
		
		try {
			String line;
			
			//"skip" first line
			line = readLine();
			checkFormat(line);
			lines.offer(line);
			
			//read interval
			line = readLine();
			checkFormat(line);
			interval = getTime(line);
			if (interval < 0) {
				throw new TSLoaderFormatException("tsl_negative_time", "Zero or negative time value.", 2, line);
			}
			if (interval == 0) {
				throw new TSLoaderFormatException("tsl_zero_time", "Zero time on second line.", lineNum, line);
			}
			lines.offer(line);
		} catch (IOException ioe) {
			throw new TSLoaderException("tsl_io", "IO error", ioe);
		}
		
		//initialize lineNum
		lineNum = 0;
	}
	
	/**
	 * Reads one line from the input. Used in @link {@link AbstractLineTSLoader#initialize()} to read first lines (such as column names).
	 * 
	 * @return One line of input.
	 * @throws IOException when input error has occurred.
	 */
	protected String readLine() throws IOException{
		return input.readLine();
	}

	/**
	 * Used by derived classes to set line number in <code>TSLoaderFormatException</code>.
	 * @return Number of processed line (from 0).
	 */
	protected int getLineNum() {
		return lineNum;
	}
	
	@Override
	public double getInterval() {
		return interval;
	}

	@Override
	public TimeSeriesPoint readPoint() throws TSLoaderException {
		String line = lines.poll();
		if (line == null) {
			try {
				line = readLine();
			} catch (IOException ioe) {
				throw new TSLoaderException("tsl_io", "IO error", ioe);
			}
		}
		if (line == null) {
			return null;
		}
		checkFormat(line);
	
		double concentration = getConcentration(line);
		if (concentration < 0) {
			throw new TSLoaderFormatException("tsl_negative_concentration", "Negative concentration.", lineNum, line);
		}
			
		lineNum++;
		return new TimeSeriesPoint(concentration, getDerivative(line));
	}
	
	/**
	 * Derived time series loader own initialization
	 * 
	 * @throws TSLoaderException when input error or another exception is encountered.
	 */
	protected abstract void initialize() throws TSLoaderException;

	/**
	 * Recovers time value of point from input line. 
	 * 
	 * @param line One line of input.
	 * @return Time value.
	 * @throws TSLoaderFormatException when input error or wrong input format is encountered. 
	 */
	protected abstract double getTime(String line) throws TSLoaderFormatException;
	
	/**
	 * Recovers species concentration value of point from input line. 
	 * 
	 * @param line One line of input.
	 * @return Species concentration value.
	 * @throws TSLoaderFormatException when wrong input format is encountered.
	 */
	protected abstract double getConcentration(String line) throws TSLoaderFormatException;
	
	/**
	 * Recovers species concentration derivative of point from input line.
	 * 
	 * @param line One line of input
	 * @return Species concentration derivative
	 * @throws TSLoaderFormatException when wrong input format is encountered.
	 */
	protected abstract double getDerivative(String line) throws TSLoaderFormatException;
	
	/**
	 *	Checks format of one input line. In the case of wrong format throws exception. 
	 * 
	 * @param line One line of input
	 * @throws TSLoaderFormatException when wrong input format is encountered.
	 */
	protected abstract void checkFormat(String line) throws TSLoaderFormatException;
}