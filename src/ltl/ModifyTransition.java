//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

import selector.Selector;
import coordinates.Transformation;

/**
 * Modifies {@link Transition} in a model. 
 * 
 * @author Tomáš Vejpustek
 *
 */
public class ModifyTransition implements ModelChange {
	private Transition original, target;
	private int index;
	
	/**
	 * Prepares transition modification.
	 * @param original state of modified {@link Transition} before this modification.
	 * @param changed modified {@link Transition}. 
	 */
	public ModifyTransition(Transition original, Transition changed) {
		this.original = original;
		target = changed;
	}

	/**
	 * @return state of modified {@link Transition} before this modification.
	 */
	public Transition getOriginal() {
		return original;
	}
	
	@Override
	public void apply(Model target) {
		index = target.modifyTransition(this.target);
	}

	@Override
	public Selector selector(Model target, Transformation coord) {
		return target.getTransitionSelector(index, coord);
	}
	
	@Override
	public void undo(Model target) {
		if (index < 0) {
			throw new IllegalStateException("Cannot undo unapplied change.");
		}
		target.modifyTransition(original, index);
	}

	@Override
	public void redo(Model target) {
		if (index < 0) {
			throw new IllegalStateException("Cannot redo unapplied change.");
		}
		target.modifyTransition(this.target, index);
	}
}
