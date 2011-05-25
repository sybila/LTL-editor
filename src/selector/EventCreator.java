//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package selector;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import ltl.CreateEvent;
import ltl.ModelChange;

import ltl.Event;
import coordinates.Transformation;

/**
 * Selector used to create {@link Event}s.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class EventCreator extends EventSelector {

	/**
	 * Starts cration of {@link Event} from given point.
	 * @param coord Coordinate transformatiom.
	 * @param origin Coordinates specifying starting point. 
	 */
	public EventCreator(Transformation coord, Point2D origin) {
		super(coord, new Event(coord.getTime(origin.getX()), coord.getConcentration(origin.getY())), null, null);
		getTarget().getTime().makePoint();
		getTarget().getConcentration().makePoint();
	}
	
	@Override
	public boolean contains(Point2D p) {
		return false;
	}

	@Override
	public void drag(MouseEvent e) {
		dragPoint(e);
	}

	@Override
	public ModelChange endDrag(MouseEvent e) {
		drag(e);
		getTarget().getConcentration().refreshReference();
		return new CreateEvent(getTarget());
	}

	@Override
	public boolean isDragging() {
		return true;
	}

	@Override
	public boolean objectContains(Point2D p) {
		return false;
	}

	@Override
	public boolean startDrag(MouseEvent e) {
		return false;
	}
}
