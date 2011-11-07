//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package selector;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ltl.Event;
import ltl.ModelChange;
import ltl.ModifyEvent;
import ltl.Property;
import ltl.Property.Bound;
import ltl.Transition;
import ui.Canvas;
import coordinates.Transformation;

/**
 * Selector of an {@link Event} which forms a "box", i.e. both its time and concentration are interval.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class BoxEventSelector extends EventMover {
	/**
	 * Instance of this class represents one of nine active point, "corners".
	 * 
	 * Each corner is represented by its position relative to time and concentration bound:
	 * it is place on its upper bound, its lower bound, or it is in the middle (represented by null value).
	 * 
	 * @author Tomáš Vejpustek
	 *
	 */
	private static class Corner {
		private Bound time, conc;
		
		/**
		 * Creates corner with specified bounds.
		 * @param timeBound Time bound.
		 * @param concentrationBound Concentration Bound.
		 */
		public Corner(Bound timeBound, Bound concentrationBound) {
			time = timeBound;
			conc = concentrationBound;
		}
		
		/**
		 * @return Time bound of this corner.
		 */
		public Bound getTimeBound() {
			return time;
		}
		
		/**
		 * @return Concentration bound of this corner.
		 */
		public Bound getConcentrationBound() {
			return conc;
		}

		/**
		 * @return All possible corners.
		 */
		public static Corner[] values() {
			Corner[] result = new Corner[9];
			List<Bound> bounds = new ArrayList<Bound>();
			bounds.add(Bound.UPPER);
			bounds.add(Bound.LOWER);
			bounds.add(null);
			int i = 0;
			for (Bound t : bounds) {
				for (Bound c : bounds) {
					result[i] = new Corner(t, c);
					i++;
				}
			}
			return result;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((conc == null) ? 0 : conc.hashCode());
			result = prime * result + ((time == null) ? 0 : time.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Corner))
				return false;
			Corner other = (Corner) obj;
			if (conc == null) {
				if (other.conc != null)
					return false;
			} else if (!conc.equals(other.conc))
				return false;
			if (time == null) {
				if (other.time != null)
					return false;
			} else if (!time.equals(other.time))
				return false;
			return true;
		}
	}

	private Corner dragged = null;
	
	/**
	 * Initializes included {@link Event}, {@link Transformation} and movement boundaries.
	 * @param left {@link Transition} adjacent to the {@link Event} from left.
	 * @param right {@link Transition} adjacent to the {@link Event} from right.
	 */
	public BoxEventSelector(Transformation coord, Event event, Transition left, Transition right) {
		super(coord, event, left, right);
	}
	
	/**
	 * @return Points in model coordinates associated with selectors in individual corners.
	 */
	public Map<Corner, Point2D> selectorPoints() {
		Map<Corner, Point2D> result = new HashMap<Corner, Point2D>();
		Map<Bound, Double> times = new HashMap<Bound, Double>();
		Map<Bound, Double> concs = new HashMap<Bound, Double>();
		
		Property time = getTarget().getTime();
		Property conc = getTarget().getConcentration();
		
		times.put(null, time.getCenter());
		concs.put(null, conc.getCenter());
		times.put(Bound.UPPER, (time.hasBound(Bound.UPPER) ? time.getBound(Bound.UPPER) : getTransformation().getTimeBound()));
		times.put(Bound.LOWER, (time.hasBound(Bound.LOWER) ? time.getBound(Bound.LOWER) : getTransformation().getTime(0)));
		concs.put(Bound.UPPER, (conc.hasBound(Bound.UPPER) ? conc.getBound(Bound.UPPER) : getTransformation().getConcentrationBound()));
		concs.put(Bound.LOWER, (conc.hasBound(Bound.LOWER) ? conc.getBound(Bound.LOWER) : getTransformation().getConcentration(getTransformation().getSize().getY())));
		
		for (Corner c : Corner.values()) {
			result.put(c, new Point2D.Double(times.get(c.getTimeBound()), concs.get(c.getConcentrationBound())));
		}
		return result;
	}
	
	@Override
	public void draw(Canvas c) {
		super.draw(c);
		if (!isDragging() && !isMoving()) {
			Map<Corner, Point2D> sp = selectorPoints();
			for (Corner corner : Corner.values()) {
				Point2D p = sp.get(corner);
				c.drawSelector(new Point2D.Double(getTransformation().getX(p.getX()), getTransformation().getY(p.getY())));
			}
		}
	}
	
	@Override
	public boolean contains(Point2D p) {
		Map<Corner, Point2D> sp = selectorPoints();
		for (Corner c : Corner.values()) {
			if (c.equals(new Corner(null, null))) {continue;}
			if (selectorBounds(sp.get(c).getX(), sp.get(c).getY()).contains(p)) {
				return true;
			}
		}
		return getDerivativeSelector().contains(p);
	}

	@Override
	public boolean startDrag(MouseEvent e) {
		if (super.startDrag(e)) {
			Point2D p = new Point2D.Double(e.getX(), e.getY());
			Map<Corner, Point2D> sp = selectorPoints();
			for (Corner c : Corner.values()) {
				if (c.equals(new Corner(null, null))) {continue;}
				if (selectorBounds(sp.get(c).getX(), sp.get(c).getY()).contains(p)) {
					dragged = c; 
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
			throw new IllegalArgumentException("Cannot continue dragging when it was not initiated.");
		}
		if (getDerivativeSelector().isDragging()) {
			getDerivativeSelector().drag(e);
		} else {
			if (dragged.getTimeBound() != null) {
				dragEdge(getTarget().getTime(), dragged.getTimeBound(), getTransformation().getTime(e.getX()), getTransformation().getTimeBound(), e.isShiftDown());
			}
			if (dragged.getConcentrationBound() != null) {
				dragEdge(getTarget().getConcentration(), dragged.getConcentrationBound(), getTransformation().getConcentration(e.getY()), getTransformation().getConcentrationBound(), e.isShiftDown());
			}
		}
	}

	@Override
	public ModelChange endDrag(MouseEvent e) {
		ModifyEvent result = (ModifyEvent)super.endDrag(e);
		if (getDerivativeSelector().isDragging()) {
			getDerivativeSelector().endDrag(e);
		} else {
		//if they are too close make them a point
			if (!e.isShiftDown()) { //shift is covered by drag
				if ((dragged.getTimeBound() != null) && getTarget().getTime().hasBounds()) {
					double upper = getTransformation().getX(getTarget().getTime().getBound(Bound.UPPER));
					double lower = getTransformation().getX(getTarget().getTime().getBound(Bound.LOWER));
					if (Math.abs(upper-lower) < Canvas.SELECTOR_SIDE) {
						getTarget().getTime().makePoint();
					}
				}
				if ((dragged.getConcentrationBound() != null) && getTarget().getConcentration().hasBounds()) {
					double upper = getTransformation().getConcentration(getTarget().getConcentration().getBound(Bound.UPPER));
					double lower = getTransformation().getConcentration(getTarget().getConcentration().getBound(Bound.LOWER));
					if (Math.abs(upper-lower) < Canvas.SELECTOR_SIDE) {
						getTarget().getConcentration().makePoint();
					}
				}
			}
			result = new ModifyEvent(result.getOriginal(), getTarget());
		}
		return result;
	}
}
