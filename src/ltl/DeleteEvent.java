//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

import coordinates.Transformation;
import ltl.Model;
import selector.Selector;

/**
 * Deletes {@link Event} from the model.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class DeleteEvent implements ModelChange {
	@SuppressWarnings("unused")
	private Event target;
	@SuppressWarnings("unused")
	private int index;
	
	/**
	 * Prepares event deleting.
	 * @param target event to be deleted.
	 */
	public DeleteEvent(Event target) {
		this.target = target;
	}
	
	@Override
	public void apply(Model target) {
		index = target.deleteEvent();

	}

	@Override
	public Selector selector(Model target, Transformation coord) {
		return null;
	}

}
