//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package selector;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ui.Canvas;
import coordinates.Transformation;

/**
 * Integrates coordinate {@link Transformation} into Selector and provides transformation primitives.
 * 
 * @author Tomáš Vejpustek
 */
public abstract class AbstractSelector implements Selector {
	private Transformation coord;

	/**
	 * Creates selector with given {@link Transformation}.
	 */
	protected AbstractSelector(Transformation coord) {
		this.coord = coord;
	}
	
	/**
	 * Transforms point in on-screen coordinates into model coordinates.
	 * @param p on-screen point.
	 * @return Point in model coordinates.
	 */
	protected Point2D getModelCoordinates(Point2D p) {
		return new Point2D.Double(coord.getTime(p.getX()), coord.getConcentration(p.getY()));
	}
	
	/**
	 * Transforms coordinates of MouseEvent into model coordinates.
	 * @return Model coordinates of <code>e</code>.
	 */
	protected Point2D getModelCoordinates(MouseEvent e) {
		return getModelCoordinates(getPoint(e));
	}
	
	/**
	 * @return Contained coordinate transformation.
	 */
	protected Transformation getTransformation() {
		return coord;
	}

	/**
	 * @param time Time coordinate.
	 * @param concentration Concentration coordinate.
	 * @return Bounding rectangle of selector at given coordinates in on-screen coordinates.
	 */
	protected Rectangle2D selectorBounds(double time, double concentration) {
		double x = getTransformation().getX(time);
		double y = getTransformation().getY(concentration);
		return selectorBounds(new Point2D.Double(x, y));
	}
	
	/**
	 * @param p Point in on-screen coordinates. 
	 * @return Bounding rectangle of selector at given on-screen coordinates in on-screen coordinates.
	 */
	protected Rectangle2D selectorBounds(Point2D p) {
		return new Rectangle2D.Double(p.getX()-Canvas.SELECTOR_SIDE/2, p.getY()-Canvas.SELECTOR_SIDE/2, Canvas.SELECTOR_SIDE, Canvas.SELECTOR_SIDE);
	}
	
	/**
	 * Transforms {@link MouseEvent} into {@link Point2D} (both in on-screen coordinates).
	 */
	protected static Point2D getPoint(MouseEvent e) {
		return new Point2D.Double(e.getX(), e.getY());
	}
}
