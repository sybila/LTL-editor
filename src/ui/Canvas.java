//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import ltl.Event;
import ltl.Property;
import ltl.Transition;
import ltl.Property.Bound;
import selector.Selector;
import series.TimeSeries;
import series.TimeSeriesIterator;
import series.TimeSeriesPoint;
import coordinates.Transformation;

/**
 * Renders graphical primitives from LTL model, time series and others.
 * 
 * Contains values used for rendering.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class Canvas {
	/** side of a selector */
	public static final double SELECTOR_SIDE = 7;
	/** side of cross signifying the property is not set */
	public static final double CROSS_SIDE = 3;
	/** length of line signifying an and of an interval */
	public static final double INT_END = 5;
	/** length of line displayed as a derivative */
	public static final double DERIVATIVE_RADIUS = 50;
	private static final double DERIVATIVE_CIRCLE_RADIUS = 25;

	private static Color BLANK = Color.WHITE;
	private static Stroke SERIES_STROKE = new BasicStroke(1);
	private static Paint SERIES_PAINT = Color.BLACK;
	private static Stroke SELECTOR_LINE_STROKE = new BasicStroke(1);
	private static Paint SELECTOR_LINE_PAINT = Color.BLACK;
	private static Paint SELECTOR_FILL_PAINT = Color.GREEN;
	private static Paint SELECTOR_FILL_INACTIVE = new Color(255, 64, 64);
	private static Stroke EVENT_LINE_STROKE = new BasicStroke(1);
	private static Color EVENT_LINE_COLOR = Color.BLACK;
	private static Color EVENT_FILL_COLOR = new Color(255, 0, 0, 128);
	private static Stroke TRANSITION_CONNECTION_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float [] {5, (float) 2.5}, 0);
	private static Color TRANSITION_CONNECTION_COLOR = new Color(0, 192, 0);
	private static Color TRANSITION_FILL_COLOR = new Color(0, 255, 0, 128);
	private static Color TRANSITION_COLOR = Color.GREEN;
	private static Stroke TRANSITION_STROKE = new BasicStroke(1);
	private static Color BLANK_GRADIENT = new Color(255, 255, 255, 0);
	private static Stroke DERIVATIVE_STROKE = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float [] {5, 5}, 0);
	private static Color DERIVATIVE_COLOR = new Color(255, 192, 0);
	private static Color ANCHOR_COLOR = Color.GRAY;
	private static Stroke ANCHOR_STROKE = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float [] {(float) 2.5, (float) 2.5}, 0);
	
	private Graphics2D canvas;
	private Transformation coord;
	private Point2D size;
	private boolean selectorsActive;
	
	/**
	 * Creates canvas from a {@link Graphics} object and clears it.
	 * 
	 * @param g Graphics object
	 * @param coordinates Coordinate transformation from parent component to model.
	 * @param width Width of parent component.
	 * @param height Height of parent component.
	 * @param selectorsActive <code>false</code> when all selectors should be drawn as disabled, <code>true</code> otherwise.
	 */
	public Canvas(Graphics g, Transformation coordinates, int width, int height, boolean selectorsActive) {
		canvas = (Graphics2D) g;
		coord = coordinates;
		size = new Point2D.Double(width, height);
		this.selectorsActive = selectorsActive;
		
		canvas.setBackground(BLANK); //sets background
		g.clearRect(0, 0, width, height);
	}
	
	/**
	 * Renders time series as a line.
	 * @param series Input time series.
	 */
	public void drawTimeSeries(TimeSeries series) {
		canvas.setPaint(SERIES_PAINT);
		canvas.setStroke(SERIES_STROKE);
		
		Point2D start, end;
		TimeSeriesIterator iter = series.iterator();
		if (!iter.hasNext()) {return;} //for the case of empty time series
		start = new Point2D.Double(0, iter.next().getConcentration());
		TimeSeriesPoint point;
		while (null != (point = iter.next())) {
			end = new Point2D.Double(point.getTime(), point.getConcentration());
			canvas.draw(new Line2D.Double(coord.getX(start.getX()), coord.getY(start.getY()), coord.getX(end.getX()), coord.getY(end.getY())));
			start = end;
		}
	}
	
	/**
	 * Renders selector -- a green square. Used generally by {@link Selector#draw(Canvas)}.
	 * @param center Center of selector in on-screen coordinates.
	 */
	public void drawSelector(Point2D center) {
		drawSelector(center, true);
	}
	
	/**
	 * Renders selector -- a square. It may be either active (and is rendered green) or inactive (red).
	 * @param center Center of selector in on-screen coordinates.
	 * @param active <code>true</code> when selector should be rendered green and <code>false</code> when red.
	 */
	public void drawSelector(Point2D center, boolean active) {
		double x = center.getX()-SELECTOR_SIDE/2;
		double y = center.getY()-SELECTOR_SIDE/2;
		Rectangle2D selector = new Rectangle2D.Double(x, y, SELECTOR_SIDE, SELECTOR_SIDE);
		if (active && selectorsActive) {
			canvas.setPaint(SELECTOR_FILL_PAINT);
		} else {
			canvas.setPaint(SELECTOR_FILL_INACTIVE);
		}
		canvas.fill(selector);
		canvas.setPaint(SELECTOR_LINE_PAINT);
		canvas.setStroke(SELECTOR_LINE_STROKE);
		canvas.draw(selector);
	}
	
	/**
	 * Renders <code>event</code> with respect to its type.
	 */
	public void drawEvent(Event event) {
		Property time = event.getTime();
		Property conc = event.getConcentration();
		Point2D ref = new Point2D.Double(coord.getX(time.getCenter()), coord.getY(conc.getCenter()));
		
		canvas.setStroke(EVENT_LINE_STROKE);
		canvas.setPaint(EVENT_LINE_COLOR);
		if (!time.isSet() || !conc.isSet()) {
			canvas.draw(new Line2D.Double(ref.getX()-CROSS_SIDE, ref.getY()-CROSS_SIDE, ref.getX()+CROSS_SIDE, ref.getY()+CROSS_SIDE));
			canvas.draw(new Line2D.Double(ref.getX()-CROSS_SIDE, ref.getY()+CROSS_SIDE, ref.getX()+CROSS_SIDE, ref.getY()-CROSS_SIDE));
		}
		
		if (!time.isSet() || time.isPoint()) { // |-type
			if (time.isPoint()) {
				canvas.draw(new Line2D.Double(ref.getX(), ref.getY()-INT_END, ref.getX(), ref.getY()+INT_END));
			}
			if (conc.isPoint()) { //star or cross
				canvas.draw(new Line2D.Double(ref.getX()-INT_END, ref.getY(), ref.getX()+INT_END, ref.getY()));
			} else if (conc.isSet()) { //interval
				if (time.isPoint()) {
					canvas.draw(new Line2D.Double(ref.getX()-CROSS_SIDE, ref.getY(), ref.getX()+CROSS_SIDE, ref.getY()));
				}
				double lower = (conc.hasBound(Bound.LOWER) ? coord.getY(conc.getBound(Bound.LOWER)) : size.getY());
				double upper = (conc.hasBound(Bound.UPPER) ? coord.getY(conc.getBound(Bound.UPPER)) : 0);
				if (conc.hasBound(Bound.UPPER)) {
					canvas.draw(new Line2D.Double(ref.getX()-INT_END, upper, ref.getX()+INT_END, upper));
				} 
				if (conc.hasBound(Bound.LOWER)) {
					canvas.draw(new Line2D.Double(ref.getX()-INT_END, lower, ref.getX()+INT_END, lower));
				} else {
					canvas.setPaint(new GradientPaint(0, (float)ref.getY(), EVENT_LINE_COLOR, 0, (float)lower, BLANK_GRADIENT, false));
				}
				if (!conc.hasBound(Bound.UPPER)) {
					canvas.setPaint(new GradientPaint(0, (float)upper, BLANK_GRADIENT, 0, (float)ref.getY(), EVENT_LINE_COLOR));
				}
				
				canvas.draw(new Line2D.Double(ref.getX(), upper, ref.getX(), lower));
			}
			drawDerivative(event.getDerivative(), new Point2D.Double(event.getTime().getCenter(), event.getConcentration().getCenter()));
		} else { //time as interval
			double timeLower = (time.hasBound(Bound.LOWER) ? coord.getX(time.getBound(Bound.LOWER)) : 0);
			double timeUpper = (time.hasBound(Bound.UPPER) ? coord.getX(time.getBound(Bound.UPPER)) : size.getX());
			if (!conc.isSet() || conc.isPoint()) { //--type
				if (conc.isPoint()) {
					canvas.draw(new Line2D.Double(ref.getX(), ref.getY()-CROSS_SIDE, ref.getX(), ref.getY()+CROSS_SIDE));
				}
				canvas.draw(new Line2D.Double(ref.getX()-INT_END,ref.getY(),ref.getX()+INT_END,ref.getY()));
				if (time.hasBound(Bound.UPPER)) {
					canvas.draw(new Line2D.Double(timeUpper, ref.getY()-INT_END, timeUpper, ref.getY()+INT_END));
				}
				if (time.hasBound(Bound.LOWER)) {
					canvas.draw(new Line2D.Double(timeLower, ref.getY()-INT_END, timeLower, ref.getY()+INT_END));
				} else {
					canvas.setPaint(new GradientPaint((float)ref.getX(), 0, EVENT_LINE_COLOR, (float)timeLower, 0, BLANK_GRADIENT));
				}
				if (!time.hasBound(Bound.UPPER)) {
					canvas.setPaint(new GradientPaint((float)timeLower, 0, BLANK_GRADIENT, (float)ref.getX(), 0, EVENT_LINE_COLOR));
				}

				canvas.draw(new Line2D.Double(timeUpper, ref.getY(), timeLower, ref.getY()));
				drawDerivative(event.getDerivative(), new Point2D.Double(event.getTime().getCenter(), event.getConcentration().getCenter()));
			} else { //square
				//other way round as (0,0) is in another corner
				double concUpper = (conc.hasBound(Bound.LOWER) ? coord.getY(conc.getBound(Bound.LOWER)) : size.getY());
				double concLower = (conc.hasBound(Bound.UPPER) ? coord.getY(conc.getBound(Bound.UPPER)) : 0);
				
				if (time.hasBounds() && conc.hasBounds()) { //square
					canvas.setPaint(EVENT_FILL_COLOR);
					canvas.fill(new Rectangle2D.Double(timeLower, concLower, timeUpper-timeLower, concUpper-concLower));
					canvas.setPaint(EVENT_LINE_COLOR);
					canvas.draw(new Rectangle2D.Double(timeLower, concLower, timeUpper-timeLower, concUpper-concLower));
				} else { //unbound square
					canvas.setPaint(EVENT_FILL_COLOR);
					canvas.fill(new Rectangle2D.Double(timeLower, concLower, timeUpper-timeLower, concUpper-concLower));
					canvas.setPaint(EVENT_LINE_COLOR);
					canvas.draw(new Rectangle2D.Double(timeLower, concLower, timeUpper-timeLower, concUpper-concLower));
				}
				
				drawDerivative(event.getDerivative(), new Point2D.Double(event.getTime().getCenter(), event.getConcentration().getCenter()));
				
				//draw a cross
				canvas.setPaint(EVENT_LINE_COLOR);
				canvas.draw(new Line2D.Double(ref.getX(), ref.getY()-CROSS_SIDE, ref.getX(), ref.getY()+CROSS_SIDE));
				canvas.draw(new Line2D.Double(ref.getX()-CROSS_SIDE, ref.getY(), ref.getX()+CROSS_SIDE, ref.getY()));
			}
		}
	} 

	/**
	 * Renders <code>trans</code> with respect to its type.
	 */
	public void drawTransition(Transition trans) {
		Point2D l, r;
		if (trans.getLeft() != null) {
			l = new Point2D.Double(coord.getX(trans.getLeft().getTime().getCenter()), coord.getY(trans.getLeft().getConcentration().getCenter()));
		} else {
			l = new Point2D.Double(0, coord.getSize().getY()/2);
		}
		if (trans.getRight() != null) {
			r = new Point2D.Double(coord.getX(trans.getRight().getTime().getCenter()), coord.getY(trans.getRight().getConcentration().getCenter()));
		} else {
			r = new Point2D.Double(coord.getSize().getX(), coord.getSize().getY()/2);
		}
		
		canvas.setStroke(TRANSITION_CONNECTION_STROKE);
		canvas.setPaint(TRANSITION_CONNECTION_COLOR);
		canvas.draw(new Line2D.Double(l, r));
		
		if (trans.getConcentration().isSet()) {
			Property conc = trans.getConcentration();
			double upper = (conc.hasBound(Bound.UPPER)) ? coord.getY(conc.getBound(Bound.UPPER)) : 0;
			double lower = (conc.hasBound(Bound.LOWER)) ? coord.getY(conc.getBound(Bound.LOWER)) : coord.getSize().getY();
			canvas.setStroke(TRANSITION_STROKE);
			if (trans.getConcentration().hasBounds()) {
				canvas.setPaint(TRANSITION_FILL_COLOR);
			} else if (trans.getConcentration().hasBound(Bound.UPPER)) {
				canvas.setPaint(new GradientPaint((float)l.getX(), (float)upper, TRANSITION_FILL_COLOR, (float)l.getX(), (float)lower, BLANK_GRADIENT));
			} else {
				canvas.setPaint(new GradientPaint((float)l.getX(), (float)lower, TRANSITION_FILL_COLOR, (float)l.getX(), (float)upper, BLANK_GRADIENT));
			}
			Rectangle2D box = new Rectangle2D.Double(l.getX(), upper, r.getX()-l.getX(), lower-upper);
			canvas.fill(box);
			canvas.setPaint(TRANSITION_COLOR);
			canvas.draw(box);
		}
		double x = coord.getTime((l.getX()+r.getX())/2);
		double y = coord.getConcentration((l.getY()+r.getY())/2);
		drawDerivative(trans.getDerivative(), new Point2D.Double(x, y));
	}

	/**
	 * Renders <code>derivative</code>.
	 * @param center Point in which the derivative should be drawn in model coordinates.
	 */
	public void drawDerivative(Property derivative, Point2D center) {
		canvas.setStroke(DERIVATIVE_STROKE);
		canvas.setPaint(DERIVATIVE_COLOR);
		Map<Bound, Double> angles = new HashMap<Bound, Double>();
		
		for (Bound b : Bound.values()) {
			if (derivative.hasBound(b)) {
				double k = derivative.getBound(b);
				double dx = (coord.getTime(coord.getX(center.getX())+DERIVATIVE_RADIUS)-center.getX())/(Math.sqrt(k*k+1));
				double dy = (dx != 0) ? k*dx : DERIVATIVE_RADIUS;
				double x = coord.getX(center.getX()+dx);
				double y = coord.getY(center.getY()+dy);
				canvas.draw(new Line2D.Double(x, y, coord.getX(center.getX()-dx), coord.getY(center.getY()-dy)));
				if (derivative.hasBounds()) {
					angles.put(b, Math.toDegrees(Math.atan((y-coord.getY(center.getY()))/(x-coord.getX(center.getX())))));
				}
			}
		}
		
		if (derivative.hasBounds()) {
			Rectangle2D bounds = new Rectangle2D.Double(coord.getX(center.getX())-DERIVATIVE_CIRCLE_RADIUS, coord.getY(center.getY())-DERIVATIVE_CIRCLE_RADIUS, 2*DERIVATIVE_CIRCLE_RADIUS, 2*DERIVATIVE_CIRCLE_RADIUS); 
			double dAngle = Math.abs(angles.get(Bound.UPPER)-angles.get(Bound.LOWER));
			if (derivative.getBound(Bound.UPPER) < derivative.getBound(Bound.LOWER)) {
				dAngle = 180 - dAngle;
			}
			canvas.draw(new Arc2D.Double(bounds, -angles.get(Bound.LOWER), dAngle, Arc2D.OPEN));
			canvas.draw(new Arc2D.Double(bounds, -angles.get(Bound.LOWER)+180, dAngle, Arc2D.OPEN));
		}
	}

	/**
	 * Draws anchors (lines to axes) for <code>e</code>. Anchor is drawn when the event is point in given coordinate
	 * (as opposed to being unset). Anchors for interval are not drawn, as it is clearly set.
	 */
	public void drawAnchors(Event e) {
		canvas.setPaint(ANCHOR_COLOR);
		canvas.setStroke(ANCHOR_STROKE);
		
		double x = coord.getX(e.getTime().getCenter());
		double y = coord.getY(e.getConcentration().getCenter());
		if (e.getConcentration().isPoint()) {
			canvas.draw(new Line2D.Double(x, y, 0, y));
		}
		if (e.getTime().isPoint()) {
			canvas.draw(new Line2D.Double(x, y, x, coord.getSize().getX()));
		}
	}
}