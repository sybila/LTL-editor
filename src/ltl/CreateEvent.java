//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

import coordinates.Transformation;
import ltl.Model;
import selector.Selector;

/**
 * Adds an {@link Event} to the model.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class CreateEvent implements ModelChange {
	private Event target;
	private EventLocation info = null;

	/**
	 * Prepares <code>target</code> to be added to a model.
	 */
	public CreateEvent(Event target) {
		this.target = target;
	}
	
	@Override
	public void apply(Model target) {
		info = target.addEvent(this.target);
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
		target.removeEvent(info.getIndex(), info.getJoined());
	}

	@Override
	public void redo(Model target) {
		if (info == null) {
			throw new IllegalStateException("Cannot redo unapplied change.");
		}
		target.insertEvent(this.target, info.getIndex(), info.getLeft(), info.getRight());
	}

}
