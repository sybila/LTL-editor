//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

import coordinates.Transformation;
import selector.Selector;

/**
 * Modifies {@link Event} in a model.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class ModifyEvent implements ModelChange {
	private Event original, target;
	private int index = -1;
	
	/**
	 * Prepares event modification.
	 * 
	 * @param original state of the {@link Event} before this modification.  
	 * @param changed modified {@link Event}.
	 */
	public ModifyEvent(Event original, Event changed) {
		this.original = original;
		target = changed;
	}
	
	/**
	 * @return State of modified {@link Event} before this modification.
	 */
	public Event getOriginal() {
		return original;
	}
	
	@Override
	public void apply(Model target) {
		index = target.modifyEvent(this.target);
	}
	
	@Override
	public Selector selector(Model target, Transformation coord) {
		return target.getEventSelector(index, coord);
	}

	@Override
	public void undo(Model target) {
		if (index < 0) {
			throw new IllegalStateException("Cannot undo unapplied change.");
		}
		target.modifyEvent(original, index);
	}

	@Override
	public void redo(Model target) {
		if (index < 0) {
			throw new IllegalStateException("Cannot redo unapplied change.");
		}
		target.modifyEvent(this.target, index);
	}

}
