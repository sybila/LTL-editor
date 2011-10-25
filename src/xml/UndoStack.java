//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.
package xml;

import java.util.ArrayDeque;
import java.util.Deque;

import ltl.Model;
import ltl.ModelChange;
import selector.Selector;
import coordinates.Transformation;

/**
 * Stack of subsequent changes to the model used to implement undo. Also watches for changes from last mark (i.e. a save point).
 * 
 * Implements linear undo model -- consists of undo and redo stack:
 * <ul>
 *  <li>when change is performed, it is put onto the top of undo stack</li>
 *  <li>when undo is performed, change is moved from the undo to the redo stack</li>
 *  <li>when redo is performed, change is moved from the redo to the undo stack</li>
 *  <li>redo stack is cleared, when a regular change is performed</li>
 * </ul>
 * 
 * @author Tomáš Vejpustek
 *
 */
public class UndoStack {
	/**
	 * Mark placed in the stack. When using <code>equals</code> on two instances, it returns <code>true</code>.
	 * 
	 * @author Tomáš Vejpustek
	 *
	 */
	private static class Mark implements ModelChange {
		@Override
		public void apply(Model target) {
			throw new UnsupportedOperationException("This ModelChange is only a dummy.");
		}

		@Override
		public Selector selector(Model target, Transformation coord) {
			throw new UnsupportedOperationException("This ModelChange is only a dummy.");
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			return (obj instanceof Mark); 
		}
		
	}
	private Deque<ModelChange> undoStack = new ArrayDeque<ModelChange>();
	private Deque<ModelChange> redoStack = new ArrayDeque<ModelChange>();
	
	/**
	 * Create empty undo stack with mark on the top.
	 */
	public UndoStack() {
		undoStack.addFirst(new Mark());
	}
	
	/**
	 * Undo an {@link ModelChange} -- move it from undo to redo stack.
	 * @return Change to be undone; <code>null</code> when there is no change to undo.
	 */
	public ModelChange undo() {
		ModelChange head = undoStack.pollFirst();
		if (head == null) {
			return null;
		}
		if (head.equals(new Mark())) {
			head = undoStack.pollFirst();
			if (head == null) {
				undoStack.addFirst(new Mark());
				return null;
			}
			redoStack.addFirst(new Mark());
		}
		redoStack.addFirst(head);
		return head;
	}
	
	/**
	 * Redo a {@link ModelChange} -- move it from redo to undo stack.
	 * @return Change to be redone; <code>null</code> when there is no change to redo.
	 */
	public ModelChange redo() {
		ModelChange head = redoStack.pollFirst();
		if (head == null) {
			return null;
		}
		undoStack.addFirst(head);
		if (new Mark().equals(redoStack.peekFirst())) {
			redoStack.removeFirst();
			undoStack.addFirst(head);
		}
		return head;
	}
	
	/**
	 * Apply a {@link ModelChange} -- put it to the top of undo stack and clear redo stack.
	 * @param target change to be applied.
	 */
	public void apply(ModelChange target) {
		undoStack.addFirst(target);
		redoStack.clear();
	}
	
	/**
	 * Mark a place in stack (i.e. a save point).
	 * @see #hasChanged()
	 */
	public void mark() {
		//first has to remove all nulls from stacks
		undoStack.remove(new Mark()); //all marks are equal
		redoStack.remove(new Mark());
		undoStack.addFirst(null);
	}
	
	/**
	 * @return <code>true</code> when there has been change since the marked point, <code>false</code> otherwise.
	 * @see #mark()
	 */
	public boolean hasChanged() {
		if (undoStack.isEmpty()) {
			return true;
		}
		return !undoStack.peekFirst().equals(new Mark());
	}

}
