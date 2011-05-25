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
 * Selector of a {@link Transition} whose concentration forms an interval.
 * @author Tomáš Vejpustek
 *
 */
public class BoxTransitionSelector extends TransitionSelector {
	private Bound dragged = null;

	/**
	 * Initializes included {@link Transition} and {@link Transformation}.
	 */
	protected BoxTransitionSelector(Transformation coord, Transition target) {
		super(coord, target);
	}
	
	@Override
	public void draw(Canvas c) {
		super.draw(c);
		if (!isDragging()) {
			Property conc = getTarget().getConcentration();
			for (Bound b : Bound.values()) {
				double val;
				if (conc.hasBound(b)) {
					val = getTransformation().getY(conc.getBound(b));
				} else {
					val = b.equals(Bound.UPPER) ? 0 : getTransformation().getSize().getY(); 
				}
				c.drawSelector(new Point2D.Double(getCenter().getX(), val));
			}
			c.drawSelector(getCenter());
		}
	}
	
	@Override
	public boolean contains(Point2D p) {
		Property conc = getTarget().getConcentration();
		for (Bound b : Bound.values()) {
			double val;
			if (conc.hasBound(b)) {
				val = getTransformation().getY(conc.getBound(b));
			} else {
				val = b.equals(Bound.UPPER) ? 0 : getTransformation().getSize().getY();
			}
			if (selectorBounds(new Point2D.Double(getCenter().getX(), val)).contains(p)) {
				return true;
			}
		}
		return getDerivativeSelector().contains(p);
	}

	@Override
	public boolean startDrag(MouseEvent e) {
		if (super.startDrag(e)) {
			Property conc = getTarget().getConcentration();
			for (Bound b : Bound.values()) {
				double val;
				if (conc.hasBound(b)) {
					val = getTransformation().getY(conc.getBound(b));
				} else {
					val = b.equals(Bound.UPPER) ? 0 : getTransformation().getSize().getY();
				}
				if (selectorBounds(new Point2D.Double(getCenter().getX(), val)).contains(getPoint(e))) {
					dragged = b;
				}
			}
			if (dragged == null) {
				getDerivativeSelector().startDrag(e);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void drag(MouseEvent e) {
		if (!isDragging()) {
			throw new IllegalStateException("Cannot continue dragging operation which has not been initiated.");
		}
		if (getDerivativeSelector().isDragging()) {
			getDerivativeSelector().drag(e);
		} else {
			Property conc = getTarget().getConcentration();
			double value = getTransformation().getConcentration(e.getY());
			if (e.isShiftDown()) {
				if ((dragged.equals(Bound.LOWER) && value > conc.getCenter()) || (dragged.equals(Bound.UPPER) && value < conc.getCenter())) {
					conc.setBound(Bound.UPPER, conc.getCenter());
					conc.setBound(Bound.LOWER, conc.getCenter());
				} else {
					double d = Math.abs(value - conc.getCenter());
					if (conc.getCenter() - d <= 0) {
						conc.unbind(Bound.LOWER);
					} else {
						conc.setBound(Bound.LOWER, conc.getCenter() - d);
					}
					if (conc.getCenter() + d >= getTransformation().getConcentrationBound()) {
						conc.unbind(Bound.UPPER);
					} else {
						conc.setBound(Bound.UPPER, conc.getCenter() + d);
					}
				}
			} else {
				if (conc.hasBound(dragged.other()) && ((dragged.equals(Bound.UPPER) && conc.getBound(dragged.other()) > value) ||
						(dragged.equals(Bound.LOWER) && conc.getBound(dragged.other()) < value))) {
					conc.setBound(dragged, conc.getBound(dragged.other()));
				} else if ((dragged.equals(Bound.UPPER) && value >= getTransformation().getConcentrationBound()) ||
						(dragged.equals(Bound.LOWER) && value <= 0)) {
					conc.unbind(dragged);
				} else {
					conc.setBound(dragged, value);
				}
			}
		}
	}

	@Override
	public ModelChange endDrag(MouseEvent e) {
		ModifyTransition result = (ModifyTransition)super.endDrag(e);
		if (getDerivativeSelector().isDragging()) {
			getDerivativeSelector().endDrag(e);
		} else {
			Property conc = getTarget().getConcentration();
			if (conc.hasBounds() && (Math.abs(conc.getBound(Bound.UPPER)-conc.getBound(Bound.LOWER)) < Canvas.SELECTOR_SIDE)) {
				conc.unset();
			}
		}
		return new ModifyTransition(result.getOriginal(), getTarget());
	}
	
}
