//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.
package selector;

import java.awt.geom.Point2D;

import ltl.Event;
import ltl.ModelChange;
import ltl.ModifyEvent;
import ltl.Transition;
import coordinates.Transformation;

/**
 * Selector of an {@link Event}. Implements its moving. Has natural movement
 * boundaries from left and right.
 * 
 * @author Tomáš Vejpustek
 *
 */
public abstract class EventMover extends EventSelector {
	private double leftBound, rightBound;
	private Transition left, right;
	private Point2D origin = null;
	
	/**
	 * Initializes {@link Transformation}, {@link Event} and movement boundaries.
	 * @param left {@link Transition} adjacent to the event from left.
	 * @param right {@link Transition} adjacent to the event from right.
	 */
	protected EventMover(Transformation coord, Event target, Transition left, Transition right) {
		super(coord, target);
		this.left = left;
		this.right = right;
		Event leftEvent = left.getLeft();
		Event rightEvent = right.getRight();
		if (leftEvent != null) {
			leftBound = leftEvent.getTime().getCenter();
		} else {
			leftBound = 0;
		}
		if (rightEvent != null) {
			rightBound = rightEvent.getTime().getCenter();
		} else {
			rightBound = getTransformation().getTimeBound();
		}
	}
	
	@Override
	public ModelChange endMove(Point2D p) {
		move(p);
		ModelChange result = new ModifyEvent(getOriginal(), getTarget());
		origin = null;
		return result;
	}
	
	@Override
	public boolean isMoving() {
		return (origin != null);
	}
	
	@Override
	public void move(Point2D p) {
		if (!isMoving()) {
			throw new IllegalStateException("Cannot continue unstarted moving.");
		}
		double time = getModelCoordinates(p).getX() - origin.getX()
				+ getOriginal().getTime().getCenter();
		double conc = getModelCoordinates(p).getY() - origin.getY()
				+ getOriginal().getConcentration().getCenter();
		if (time > leftBound && time < rightBound) {
			getTarget().getTime().move(time);
		}
		if (conc > 0 && conc < getTransformation().getConcentrationBound()) {
			getTarget().getConcentration().move(conc);
		}
	}
	
	@Override
	public boolean startMove(Point2D p) {
		if (isMoving() || isDragging()) {
			throw new IllegalStateException(
					"Cannot start moving when already moving or dragging.");
		}
		if (objectContains(p)) {
			origin = getModelCoordinates(p);
			left.setRight(getTarget());
			right.setLeft(getTarget());
			return true;
		}
		return false;
	}
	
}
