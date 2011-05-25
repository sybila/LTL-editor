//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package selector;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ltl.Property;
import ui.Canvas;
import coordinates.Transformation;

/**
 * Special selector for species derivative (independent of underlying primitive).
 * 
 * Note: This is not an implementation of Selector class. It is intended to be included in {@link EventSelector}s
 * and {@link TransitionSelector}s. Consequently, storing of previous underlying primitive version and this class
 * operation is handled by such classes. 
 * 
 * @author Tomáš Vejpustek
 *
 */
public abstract class DerivativeSelector {
	private Property target;
	private Transformation coord;
	private boolean dragging = false;
	private Point2D center;
	
	/**
	 * Returns appropriate selector for <code>derivative</code>.
	 * @param coord Coordinate transformation from model to on-screen coordinates.
	 * @param center Central point of <code>derivative</code> in model coordinates -- its position. 
	 */
	public static DerivativeSelector get(Transformation coord, Property derivative, Point2D center) {
		if (!derivative.isSet()) {
			return new PointDerivativeSelector(coord, derivative, center);
		} else if (derivative.isPoint()) { 
			return new LineDerivativeSelector(coord, derivative, center);
		} else if (derivative.hasBounds()) {
			return new AngleDerivativeSelector(coord, derivative, center);
		} else {
			return null;
		}
	}

	/**
	 * Creates derivative selector 
	 * @param coord transformation from on-screen to model coordinates.
	 * @param derivative property representing derivative
	 * @param center Central point of <code>derivative</code> in model coordinates -- its position.
	 */
	protected DerivativeSelector(Transformation coord, Property derivative, Point2D center) {
		this.coord = coord;
		target = derivative;
		this.center = center;
	}
	
	/**
	 * @return Derivative property being modified.
	 */
	protected Property getTarget() {
		return target;
	}
	
	/**
	 * @return Underlying transformation from on-screen to model coordinates.
	 */
	protected Transformation getTransformation() {
		return coord;
	}

	/**
	 * @return <code>true</code> when <code>p</code> is inside derivative selector's active parts, <code>false</code> otherwise.
	 */
	public abstract boolean contains(Point2D p);

	/**
	 * Continues dragging operation in given point.
	 * @param e mouse event specifying target point.
	 * @throws IllegalStateException when not dragging.
	 */
	public void drag(MouseEvent e) {
		if (!isDragging()) {
			throw new IllegalStateException("Cannot continue dragging operation which was not initiated.");
		}
	}

	/**
	 * @return <code>true</code> when derivative selector performs dragging operation, <code>false</code> otherwise.
	 */
	public boolean isDragging() {
		return dragging;
	}

	/**
	 * Starts dragging operation from given point.
	 * @param e mouse event specifying starting point.
	 * @return <code>true</code> if drag operation was started, <code>false</code> otherwise (i.e. dragging outside of active parts, ...).
	 * @throws IllegalStateException when already dragging.
	 */
	public boolean startDrag(MouseEvent e) {
		if (isDragging()) {
			throw new IllegalStateException("Cannot initiate dragging when already dragging.");
		}
		if (contains(getPoint(e))) {
			dragging = true;
			return true;
		}
		return false;
	}

	/**
	 * Ends dragging operation in given point.
	 * @param e mouse event specifying target point.
	 * @throws IllegalStateException when not dragging.
	 */
	public void endDrag(MouseEvent e) {
		drag(e);
		dragging = false;
	}
	
	/**
	 * @return Point where the derivative is placed in model coordinates.
	 */
	protected Point2D getCenter() {
		return center;
	}
	
	/**
	 * @return Slope of line connecting point of derivative and <code>p</code>.
	 * @see #getCenter()
	 */
	protected double getDerivative(Point2D p) {
		double time = getTransformation().getTime(p.getX());
		double conc = getTransformation().getConcentration(p.getY());
		return (conc-getCenter().getY())/(time-getCenter().getX());
	}

	/**
	 * @return <code>true</code> if <code>p</code> is closer than {@link Canvas#INT_END} to line through point of derivative
	 * with the slope of <code>derivative</code>. 
	 */
	protected boolean isCloseToTangent(double derivative, Point2D p) {
		if (Double.isInfinite(derivative)) {
			double dX = getTransformation().getX(getCenter().getX())-p.getX();
			return (Canvas.INT_END > Math.abs(dX));
		} else {
			double dTime = getTransformation().getTime(p.getX())-getCenter().getX();
			double y = getTransformation().getY(getCenter().getY()+derivative*dTime);
			double k = (y-getTransformation().getY(getCenter().getY()))/(p.getX()-getTransformation().getX(getCenter().getX()));
			return (Canvas.INT_END*(Math.sqrt(k*k+1)) > Math.abs(p.getY()-y));
		}
	}

	/**
	 * @return <code>true</code> when <code>p</code> is close (screen-wise) to the center, <code>false</code> otherwise.
	 */
	protected boolean centerContains(Point2D p) {
		Rectangle2D bound = new Rectangle2D.Double(getTransformation().getX(getCenter().getX())-Canvas.SELECTOR_SIDE/2, getTransformation().getY(getCenter().getY())-Canvas.SELECTOR_SIDE/2, Canvas.SELECTOR_SIDE, Canvas.SELECTOR_SIDE);
		return bound.contains(p);
	}
	
	/**
	 * @return <code>e</code> as a {@link Point2D}.
	 */
	protected static Point2D getPoint(MouseEvent e) {
		return new Point2D.Double(e.getX(), e.getY());
	}
	
}
