//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ui;

import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import mutable.MouseAction;
import mutable.MutableMouseAction;

/**
 * Manage mouse action types (such as what to do on click) for {@link WorkSpace}. Types may be easily changed.
 * 
 * @author Tomáš Vejpustek
 */
public class MouseActionManager {
	/**
	 * All actions handled by {@link MouseActionManager}
	 * 
	 * @author Tomáš Vejpustek
	 */
	static public enum MouseActionType {
		/**	Creates point interval -- starts dragging */
		CREATE_EVENT_PRESS,
		/** edit event -- start moving or dragging */
		EDIT_MODEL_PRESS,
		/** selects event */
		SELECT_PRIMITIVE,
		/** starts moving of event */
		MOVE_EVENT_PRESS,
		/** Creates point interval -- called during dragging */
		SELECTOR_DRAG,
		/** Creates point interval -- ends dragging */
		SELECTOR_RELEASED,
		/** Deletes graphical primitive */
		DELETE_PRIMITIVE
		
	}
	
	private Map<MouseActionType, MutableMouseAction> actions = new HashMap<MouseActionType, MutableMouseAction>();
	
	/**
	 * Initializes actions
	 */
	public MouseActionManager() {
		for (MouseActionType type : MouseActionType.values()) {
			actions.put(type, new MutableMouseAction());
		}
	}
	
	/**
	 * Sets {@link MouseAction} performed by given action type.
	 * 
	 * @param type Action type.
	 * @param action Action to be performed.
	 */
	public void setMouseAction(MouseActionType type, MouseAction action) {
		actions.get(type).setMouseAction(action);
	}
	
	/**
	 * Used to set {@link MouseListener}s. When action performed by given action type changes,
	 * the change is reflected in all respective {@link MouseListener}s.  
	 * 
	 * @return {@link MouseAction} object associated with given {@link MouseActionType}.
	 */
	public MouseAction getMouseAction(MouseActionType type) {
		return actions.get(type);
	}
}
