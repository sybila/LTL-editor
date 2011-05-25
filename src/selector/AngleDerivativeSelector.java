//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package selector;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import ui.Canvas;

import ltl.Property;
import ltl.Property.Bound;

import coordinates.Transformation;

/**
 * Derivative selector when derivative has both bounds and forms "an angle".
 * 
 * @author Tomáš Vejpustek
 *
 */
public class AngleDerivativeSelector extends DerivativeSelector {
	private Bound dragged;
	private double lower, upper;
	
	/**
	 * Creates derivative selector with two bounds. 
	 * @param coord transformation from on-screen to model coordinates.
	 * @param derivative property representing derivative
	 * @param center Central point of <code>derivative</code> in model coordinates -- its position.
	 */
	protected AngleDerivativeSelector(Transformation coord, Property derivative, Point2D center) {
		super(coord, derivative, center);
		lower = derivative.getBound(Bound.LOWER);
		upper = derivative.getBound(Bound.UPPER);
	}

	@Override
	public boolean contains(Point2D p) {
		double dx = p.getX()-getTransformation().getX(getCenter().getX());
		double dy = p.getY()-getTransformation().getY(getCenter().getY());
		if (Math.sqrt(dx*dx+dy*dy) > Canvas.DERIVATIVE_RADIUS) {
			return false;
		}
		for (Bound b : Bound.values()) {
			if (isCloseToTangent(getTarget().getBound(b), p)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean startDrag(MouseEvent e) {
		if (super.startDrag(e)) {
			if (isCloseToTangent(getTarget().getBound(Bound.UPPER), getPoint(e))) {
				dragged = Bound.UPPER;
			} else {
				dragged = Bound.LOWER;
			}
			return true;
		}
		return false;
	}

	@Override
	public void drag(MouseEvent e) {
		Point2D p = getPoint(e);
		if (centerContains(p)) {
			if (e.isShiftDown()) {
				getTarget().unset();
			} else {
				getTarget().unbind(dragged);
			}
		} else {
			if (!getTarget().hasBound(Bound.UPPER)) {
				getTarget().setBound(Bound.UPPER, upper);
			}
			if (!getTarget().hasBound(Bound.LOWER)) {
				getTarget().setBound(Bound.LOWER, lower);
			}
			double d = getDerivative(p);
			getTarget().setBound(dragged, d);
			if (e.isShiftDown()) {
				double dD = d - getTarget().getCenter();
				getTarget().setBound(dragged.other(), getTarget().getCenter()-dD);
			}
		}
	}
	
	@Override
	public void endDrag(MouseEvent e) {
		super.endDrag(e);
		if (getTarget().hasBounds() && isCloseToTangent(getTarget().getBound(dragged.other()), getPoint(e))) {
			getTarget().unbind(dragged);
			getTarget().makePoint();
		} else if (!getTarget().hasBound(Bound.UPPER)) {
			getTarget().makePoint();
		}
	}
}
