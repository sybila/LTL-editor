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
	private Event target;
	private EventLocation info = null;
	
	/**
	 * Prepares event deleting.
	 * @param target event to be deleted.
	 */
	public DeleteEvent(Event target) {
		this.target = target;
	}
	
	@Override
	public void apply(Model target) {
		info = target.deleteEvent();

	}

	@Override
	public Selector selector(Model target, Transformation coord) {
		return null;
	}
	

	@Override
	public void undo(Model target) {
		if (info == null) {
			throw new IllegalStateException("Cannot undo unapplied change.");
		}
		target.insertEvent(this.target, info.getIndex(), info.getLeft(), info.getRight());
	}

	@Override
	public void redo(Model target) {
		if (info == null) {
			throw new IllegalStateException("Cannot redo unapplied change.");
		}
		target.removeEvent(info.getIndex(), info.getJoined());
	}


}
