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
 * Derivative selective when derivative is a point (i.e. one line -- tangent).
 * 
 * @author Tomáš Vejpustek
 *
 */
public class LineDerivativeSelector extends DerivativeSelector {
	private boolean fromCenter; 

	/**
	 * Creates derivative selector of point. 
	 * @param coord transformation from on-screen to model coordinates.
	 * @param derivative property representing derivative
	 * @param center Central point of <code>derivative</code> in model coordinates -- its position.
	 */
	protected LineDerivativeSelector(Transformation coord, Property derivative, Point2D center) {
		super(coord, derivative, center);
	}
	
	@Override
	public boolean contains(Point2D p) {
		double dx = p.getX()-getTransformation().getX(getCenter().getX());
		double dy = p.getY()-getTransformation().getY(getCenter().getY());
		if (Math.sqrt(dx*dx+dy*dy) > Canvas.DERIVATIVE_RADIUS) {
			return false;
		}
		return (centerContains(p) || isCloseToTangent(getTarget().getBound(Bound.UPPER), p));
	}
	
	@Override
	public boolean startDrag(MouseEvent e) {
		if (super.startDrag(e)) {
			fromCenter = centerContains(getPoint(e));
			return true;
		}
		return false;
	}
	
	@Override
	public void drag(MouseEvent e) {
		super.drag(e);
		Point2D p = getPoint(e);
		Bound target;
		if (fromCenter || e.isControlDown()) {
			target = Bound.LOWER;
		} else {
			target = Bound.UPPER;
			getTarget().makePoint();
		}
		if (centerContains(p)) {
			if (e.isShiftDown()) {
				getTarget().unset();
			} else {
				getTarget().unbind(target);
			}
		} else {
			double d = getDerivative(p);
			getTarget().setBound(target, d);
			if (e.isShiftDown()) {
				double dD = d-getTarget().getCenter();
				getTarget().setBound(target.other(), getTarget().getCenter()-dD);
			}
		}
	}

	@Override
	public void endDrag(MouseEvent e) {
		super.endDrag(e);
		if (getTarget().hasBounds()) {
			double d = (fromCenter || e.isControlDown()) ? getTarget().getBound(Bound.UPPER) : getTarget().getBound(Bound.LOWER);
			if (isCloseToTangent(d, getPoint(e))) {
				getTarget().makePoint();
			}
		}
	}
}
