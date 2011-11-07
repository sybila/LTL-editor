//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package selector;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import ltl.Event;
import ltl.ModelChange;
import ltl.ModifyEvent;
import ltl.Transition;
import ui.Canvas;
import coordinates.Transformation;

/**
 * Selector of {@link Event} whose both time and concentration are not set or form a point.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class PointEventSelector extends EventMover {
	
	/**
	 * Initializes contained {@link Event} and {@link Transformation}; sets
	 * movement boundaries.
	 * @param left {@link Transition} adjacent to the {@link Event} from left.
	 * @param right {@link Transition} adjacent to the {@link Event} from right.
	 */
	protected PointEventSelector(Transformation coord, Event event, Transition left, Transition right) {
		super(coord, event, left, right);
	}
	
	@Override
	public void draw(Canvas c) {
		super.draw(c);
		if (!isMoving() && !isDragging() && !isDragging()) {
			c.drawSelector(new Point2D.Double(getTransformation().getX(getTarget().getTime().getCenter()), getTransformation().getY(getTarget().getConcentration().getCenter())));
		}
	}
	
	@Override
	public boolean contains(Point2D p) {
		if (selectorBounds(getTarget().getTime().getCenter(), getTarget().getConcentration().getCenter()).contains(p)) {
			return true;
		}
		return getDerivativeSelector().contains(p);
	}

	@Override
	public void drag(MouseEvent e) {
		if (!isDragging()) {
			throw new IllegalStateException("Cannot continue dragging when it was not initiated.");
		}
		if (getDerivativeSelector().isDragging()) {
			getDerivativeSelector().drag(e);
		} else {
			dragPoint(e);
		}
	}

	@Override
	public ModelChange endDrag(MouseEvent e) {
		ModifyEvent result = (ModifyEvent)super.endDrag(e);
		if (getDerivativeSelector().isDragging()) {
			getDerivativeSelector().endDrag(e);
		} else if ((getTarget().getTime().isPoint() || !getTarget().getTime().isSet()) && (getTarget().getConcentration().isPoint() || !getTarget().getConcentration().isSet())) {
			Event original = result.getOriginal();
			if (getTarget().getTime().isPoint()) {
				if (getTarget().getConcentration().isPoint()) {
					getTarget().getTime().unset();
				} else { //concentration unset
					getTarget().getConcentration().makePoint();
				}
			} else { //time unset
				if (getTarget().getConcentration().isPoint()) {
					getTarget().getConcentration().unset();
				} else { //concentration unset
					getTarget().getTime().makePoint();
				}
			}
			result = new ModifyEvent(original, getTarget());
		}
		return result;
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
	
}
