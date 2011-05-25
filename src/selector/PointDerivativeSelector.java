//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package selector;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import ltl.Property;
import ltl.Property.Bound;
import coordinates.Transformation;
/**
 * Derivative selector when derivative is not set (i.e. is dragged from a point).
 * 
 * @author Tomáš Vejpustek
 *
 */
public class PointDerivativeSelector extends DerivativeSelector {
	
	/**
	 * Creates a derivative selector of not set derivative.
	 * @param coord transformation from on-screen to model coordinates.
	 * @param derivative property representing derivative
	 * @param center Central point of <code>derivative</code> in model coordinates -- its position.
	 */
	protected PointDerivativeSelector(Transformation coord, Property derivative, Point2D center) {
		super(coord, derivative, center);
	}

	@Override
	public boolean contains(Point2D p) {
		return centerContains(p);
	}

	@Override
	public void drag(MouseEvent e) {
		super.drag(e);
		Point2D p = getPoint(e);
		if (centerContains(p)) {
			getTarget().unset();
		} else {
			double d = getDerivative(p);
			getTarget().setBound(Bound.UPPER, d);
			if (e.isShiftDown()) {
				double dD = d-getTarget().getCenter();
				getTarget().setBound(Bound.LOWER, getTarget().getCenter()-dD);
			} else {
				getTarget().makePoint();
			}
		}
	}
}
