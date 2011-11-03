//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

import selector.Selector;
import coordinates.Transformation;

/**
 *  Change to a model. For transparency reasons and Undo function.
 * 
 * @author Tomáš Vejpustek
 * 
 */
public interface ModelChange {
	/**
	 * Applies change to <code>target</code>. Implement only -- use {@link Model#applyChange(ModelChange)} instead.
	 */
	public void apply(Model target);
	
	/**
	 * @return Selector of changed graphical primitive. May return <code>null</code> when no primitive may be selected (delete action, ...)
	 * @param Transformation from on-screen to model coordinates.
	 */
	public Selector selector(Model target, Transformation coord);
	
	/**
	 * @return Localized description of the change. 
	 */
	//public String getDescription();
	
	/**
	 * Revokes the change on <code>target</code>.
	 * 
	 * @throws IllegalStateException when called before {@link #apply(Model)} is called. 
	 */
	public void undo(Model target);
	
	/**
	 * Applies change on <code>target</code> after it has been undone.
	 * @see {@link #undo(Model target)}.
	 */
	public void redo(Model target);
}
