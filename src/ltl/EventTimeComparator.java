//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

import java.util.Comparator;

/**
 * Compares two {@link Event}s according to their time reference (and their position along X axis).
 * 
 * @author Tomáš Vejpustek
 *
 */
public class EventTimeComparator implements Comparator<Event> {

	@Override
	public int compare(Event arg0, Event arg1) {
		return Double.compare(arg0.getTime().getCenter(), arg1.getTime().getCenter()); 
	}

}
