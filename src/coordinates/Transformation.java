//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package coordinates;

import java.awt.geom.Point2D;

import series.TimeSeries;
import series.TimeSeriesIterator;
import series.TimeSeriesPoint;

/**
 * Transforms coordinates of a {@link ui.WorkSpace} to time series coordinates and vice versa.
 * 
 * Note: deals with on-screen origin being in upper left corner and time series origin in lower left one.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class Transformation {
	private static double PADDING = 50;
	private Scale timeScale, concScale;
	private double height, width;
	
	
	/**
	 * Creates identity on given component.
	 * @param height Height of parent component.
	 * @param width Width of parent component.
	 */
	public Transformation(double width, double height) {
		setIdentity();
		this.height = height;
		this.width = width;
	}
	
	/**
	 *	Creates transformation with given time scale and concentration scale.
	 * 
	 * @param timeScale Time scale.
	 * @param concentrationScale Concentration scale.
	 * @param width Width of parent component.
	 * @param height Height of parent component.
	 */
	public Transformation(Scale timeScale, Scale concentrationScale, double width, double height) {
		this.timeScale = timeScale;
		concScale = concentrationScale;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Resizes transformation to new size of parent component. Resizes its time and concentration scales as well.
	 * 
	 * @param newWidth New width of the parent component.
	 * @param newHeight New height of the parent component.
	 */
	public void resize(double newWidth, double newHeight) {
		timeScale.resize(0, 0, width, newWidth);
		concScale.resize(0, 0, height, newHeight);
		width = newWidth;
		height = newHeight;
	}
	
	/**
	 * Used when a new time series is loaded to set transformation accordingly.
	 * @param series Loaded time series.
	 */
	public void setLinearTransformation(TimeSeries series) {
		Scale timeScale = new LinearScale(width, series.getLength());
		
		double max = 0;
		TimeSeriesIterator iter = series.iterator();
		
		while (iter.hasNext()) {
			TimeSeriesPoint point = iter.next();
			if (point.getConcentration() > max) {
				max = point.getConcentration();
			}
		}
		Scale concScale = new LinearScale(height-PADDING, max);
		setTimeScale(timeScale);
		setConcentrationScale(concScale);
	}
	
	/**
	 * Used when time series is cleared to set transformation accordingly.
	 */
	public void setIdentity() {
		timeScale = new LinearScale();
		concScale = new LinearScale();
	}

	/**
	 * @return Value of X coordinate from time on time series. 
	 */
	public double getX(double time) {
		return timeScale.getBase(time);
	}
	
	/**
	 * @return Value of Y coordinate from species concentration on time series.
	 */
	public double getY(double concentration) {
		return height-concScale.getBase(concentration);
	}
	
	/**
	 * @return Time on time series from X coordinate.
	 */
	public double getTime(double x) {
		return timeScale.getScaled(x);
	}
	
	/**
	 * @return Species concentration on time series from 
	 */
	public double getConcentration(double y) {
		return concScale.getScaled(height-y);
	}
	
	/**
	 * @return Maximum time value fitting into parent component.
	 */
	public double getTimeBound() {
		return getTime(width);
	}
	
	/**
	 * @return Maximum concentration value fitting into parent component.
	 */
	public double getConcentrationBound() {
		return getConcentration(0);
	}
	
	/**
	 * @return Transformation time scale.
	 */
	public Scale getTimeScale() {
		return timeScale;
	}
	
	/**
	 * @return Transformation concentration scale.
	 */
	public Scale getConcentrationScale() {
		return concScale;
	}

	/**
	 * Changes time scale of concentration.
	 * @param timeScale New value of time scale.
	 */
	public void setTimeScale(Scale timeScale) {
		this.timeScale = timeScale;
	}

	/**
	 * Changes concentration scale of concentration.
	 * @param concScale New value of concentration scale.
	 */
	public void setConcentrationScale(Scale concScale) {
		this.concScale = concScale;
	}
	
	/**
	 * @return Size of the parent component.
	 */
	public Point2D getSize() {
		return new Point2D.Double(width, height);
	}

}
