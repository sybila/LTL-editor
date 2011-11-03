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
	@SuppressWarnings("unused")
	private int index;

	/**
	 * Prepares <code>target</code> to be added to a model.
	 */
	public CreateEvent(Event target) {
		this.target = target;
	}
	
	@Override
	public void apply(Model target) {
		index = target.addEvent(this.target);
	}

	@Override
	public Selector selector(Model target, Transformation coord) {
		return null;
	}

	@Override
	public void undo(Model target) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	@Override
	public void redo(Model target) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not yet implemnted.");
	}

}
