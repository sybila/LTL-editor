//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package selector;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import ltl.ModelChange;
import ltl.ModifyTransition;
import ltl.Property;
import ltl.Transition;
import ltl.Property.Bound;
import ui.Canvas;
import coordinates.Transformation;

/**
 * Selector of a {@link Transition} whose concentration is not set.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class PointTransitionSelector extends TransitionSelector {

	/**
	 * Initializes included {@link Transition} and {@link Transformation}.
	 */
	protected PointTransitionSelector(Transformation coord, Transition target) {
		super(coord, target);
	}
	
	@Override
	public void draw(Canvas c) {
		super.draw(c);
		if (!isDragging()) {
			c.drawSelector(getCenter());
		}
	}
	
	@Override
	public boolean startDrag(MouseEvent e) {
		if (super.startDrag(e)) {
			Point2D p = getPoint(e);
			if (getDerivativeSelector().centerContains(p)) {
				if (e.isAltDown()) {
					getDerivativeSelector().startDrag(e);
				}
			} else if (getDerivativeSelector().contains(p)) {
				getDerivativeSelector().startDrag(e);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean contains(Point2D p) {
		if (isDragging()) {
			return false;
		}
		if (selectorBounds(getCenter()).contains(p)) {
			return true;
		}
		return getDerivativeSelector().contains(p);
	}

	@Override
	public void drag(MouseEvent e) {
		if (!isDragging()) {
			throw new IllegalStateException("Cannot continue dragging operation when it was not initiated.");
		}
		if (getDerivativeSelector().isDragging()) {
			getDerivativeSelector().drag(e);
		} else {
			double value = getTransformation().getConcentration(e.getY());
			Property conc = getTarget().getConcentration();
			if (e.isShiftDown()) {
				double d = Math.abs(value - conc.getCenter());
				if (conc.getCenter() - d <= 0) {
					conc.unbind(Bound.LOWER);
				} else {
					conc.setBound(Bound.LOWER, conc.getCenter()-d);
				}
				if (conc.getCenter() + d >= getTransformation().getConcentrationBound()) {
					conc.unbind(Bound.UPPER);
				} else {
					conc.setBound(Bound.UPPER, conc.getCenter()+d);
				}
			} else {
				if (value >= conc.getCenter()) {
					conc.setBound(Bound.UPPER, value);
					conc.setBound(Bound.LOWER, conc.getCenter());
				} else {
					conc.setBound(Bound.LOWER, value);
					conc.setBound(Bound.UPPER, conc.getCenter());
				}
			}
		}
	}

	@Override
	public ModelChange endDrag(MouseEvent e) {
		ModifyTransition result = (ModifyTransition)super.endDrag(e);
		if (getDerivativeSelector().isDragging()) {
			getDerivativeSelector().endDrag(e);
		} else if (Math.abs(getCenter().getY() - e.getY()) <= Canvas.SELECTOR_SIDE) {
			getTarget().getConcentration().unset();
		}
		return new ModifyTransition(result.getOriginal(), getTarget()); 
	}
	
}
