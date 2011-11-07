//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package selector;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ltl.Event;
import ltl.ModelChange;
import ltl.ModifyEvent;
import ltl.Property;
import ltl.Property.Bound;
import ltl.Transition;
import ui.Canvas;
import coordinates.Transformation;

/**
 * Selector of an event with one interval property, the other being a point or not set, e.g. an interval in concentration with no time set.
 * 
 * @author Tomáš Vejpustek
 * 
 */
public class LineEventSelector extends EventMover {
	private static enum SelectorPoint {
		CENTER, //central selector (point property)
		UPPER, //upper bound selector (interval property)
		LOWER //lower bound selector (interval property)
	}
	private boolean isTimeLine;
	private boolean isPoint;
	private SelectorPoint dragged = null;
	
	/**
	 * Initializes contained {@link Event} and superclass.
	 * 
	 * @param coord Coordinate transformation from on-screen to model coordinates.
	 * @param isTimeLine <code>true</code> if event is interval in time, <code>false</code> if it is interval in concentration.
	 */
	protected LineEventSelector(Transformation coord, Event event, Transition left, Transition right, boolean isTimeLine) {
		super(coord, event, left, right);
		this.isTimeLine = isTimeLine;
	}
	
	/**
	 * @return Property (time or concentration) which forms an interval.
	 */
	private Property getLineProperty() {
		if (isTimeLine) {
			return getTarget().getTime();
		} else {
			return getTarget().getConcentration();
		}
	}
	
	/**
	 * @return Property (time or concentration) which forms a point.
	 */
	private Property getPointProperty() {
		if (isTimeLine) {
			return getTarget().getConcentration();
		} else {
			return getTarget().getTime();
		}
	}
	
	/**
	 * @return Points in model coordinates associated with selectors at the end of interval and in the middle.  
	 */
	private Point2D[] selectorPoints() {
		Point2D[] result = new Point2D[SelectorPoint.values().length];
		result[SelectorPoint.CENTER.ordinal()] = new Point2D.Double(getTarget().getTime().getCenter(), getTarget().getConcentration().getCenter());
		
		double center = getPointProperty().getCenter();
		double upper, lower;
		if (getLineProperty().hasBound(Bound.UPPER)) {
			upper = getLineProperty().getBound(Bound.UPPER);
		} else {
			if (isTimeLine) {
				upper = getTransformation().getTimeBound();
			} else {
				upper = getTransformation().getConcentrationBound();
			}
		}
		if (getLineProperty().hasBound(Bound.LOWER)) {
			lower = getLineProperty().getBound(Bound.LOWER);
		} else {
			if (isTimeLine) {
				lower = getTransformation().getTime(0);
			} else {
				lower = getTransformation().getConcentration(getTransformation().getSize().getY());
			}
		}
		if (isTimeLine) {
			result[SelectorPoint.UPPER.ordinal()] = new Point2D.Double(upper, center);
			result[SelectorPoint.LOWER.ordinal()] = new Point2D.Double(lower, center);
		} else {
			result[SelectorPoint.UPPER.ordinal()] = new Point2D.Double(center, upper);
			result[SelectorPoint.LOWER.ordinal()] = new Point2D.Double(center, lower);
		}
		return result;
	}
	
	@Override
	public void draw(Canvas c) {
		super.draw(c);
		if (!isDragging() && !isMoving()) {
			for (Point2D p : selectorPoints()) {
				c.drawSelector(new Point2D.Double(getTransformation().getX(p.getX()), getTransformation().getY(p.getY())));
			}
		}
	}
	
	@Override
	public boolean startDrag(MouseEvent e) {
		if (super.startDrag(e)) {
			Point2D p = new Point2D.Double(e.getX(), e.getY());
			Point2D[] points = selectorPoints();
			for (SelectorPoint sel : SelectorPoint.values()) {
				if (selectorBounds(points[sel.ordinal()].getX(), points[sel.ordinal()].getY()).contains(p)) {
					dragged = sel;
				}
			}
			isPoint = getPointProperty().isPoint();
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
		for (Point2D sel : selectorPoints()) {
			if (selectorBounds(sel.getX(), sel.getY()).contains(p)) {
				return true;
			}
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
			double timeCenter = (isTimeLine ? getTransformation().getTime(e.getX()) : getTarget().getTime().getCenter());
			double concCenter = (isTimeLine ? getTarget().getConcentration().getCenter() : getTransformation().getConcentration(e.getY()));
			Rectangle2D center = selectorBounds(timeCenter, concCenter);
			if (dragged.equals(SelectorPoint.CENTER)) {
				if (center.contains(new Point2D.Double(e.getX(), e.getY()))) {
					if (isPoint) {
						getPointProperty().makePoint();
					} else {
						getPointProperty().unset();
					}
				} else {
					double val = (isTimeLine ? getTransformation().getConcentration(e.getY()) : getTransformation().getTime(e.getX()));
					double d =  val - getPointProperty().getCenter();
					double maxVal = (isTimeLine ? getTransformation().getConcentrationBound() : getTransformation().getTimeBound());
					if (d > 0) {
						dragEdge(getPointProperty(), Bound.UPPER, val, maxVal, e.isShiftDown());
						if (!e.isShiftDown()) {
							getPointProperty().stretchBound(Bound.LOWER, getPointProperty().getCenter());
						}
					} else {
						dragEdge(getPointProperty(), Bound.LOWER, val, maxVal, e.isShiftDown());
						if (!e.isShiftDown()) {
							getPointProperty().stretchBound(Bound.UPPER, getPointProperty().getCenter());
						}
					}
				}
			} else {
				getPointProperty().makePoint();
				double val = (isTimeLine ? getTransformation().getTime(e.getX()) : getTransformation().getConcentration(e.getY()));
				double maxVal = (isTimeLine ? getTransformation().getTimeBound() : getTransformation().getConcentrationBound());
				if (dragged.equals(SelectorPoint.UPPER)) {
					dragEdge(getLineProperty(), Bound.UPPER, val, maxVal, e.isShiftDown());
				} else {
					dragEdge(getLineProperty(), Bound.LOWER, val, maxVal, e.isShiftDown());
				}
			}
		}
	}

	@Override
	public ModelChange endDrag(MouseEvent e) {
		ModifyEvent result = (ModifyEvent)super.endDrag(e);
		if (getDerivativeSelector().isDragging()) {
			getDerivativeSelector().endDrag(e);
		} else {
			if (dragged.equals(SelectorPoint.CENTER) && (getPointProperty().isPoint() || !getPointProperty().isSet())) {
				if (getPointProperty().isSet()) {
					getPointProperty().unset();
				} else {
					getPointProperty().makePoint();
				}
				result = new ModifyEvent(result.getOriginal(), getTarget());
			}
		}
		return result;
	}
}
