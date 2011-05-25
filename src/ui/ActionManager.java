//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ui;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Action;

import mutable.MutableAction;

/**
 * Manages action types (such as "Save File"). Action performed by given action type may be changed easily. 
 * 
 * @author Tomáš Vejpustek
 *
 */
public class ActionManager {
	/**
	 * 
	 * All actions handled by {@link ActionManager} (may not necessarily be all actions in a program).
	 * 
	 * @author Tomáš Vejpustek
	 *
	 */
	//Note: each ActionType specifies key in localization resource bundle Labels
	static public enum ActionType {
		/** Exits program */
		EXIT,
		/** Loads a time series from CSV file */
		TS_LOAD_CSV,
		/** Clears time series */
		TS_CLEAR,
		/** Saves current formula by different name */
		FORM_SAVE_AS,
		/** Saves current formula */
		FORM_SAVE,
		/** Loads a formula */
		FORM_LOAD,
		/** clears current formula */
		FORM_NEW,
		/** Sets state to creating Events */
		CREATE_EVENTS,
		/** Sets state to editing model (moving and dragging) */
		EDIT_MODEL,
		/** Sets state to moving Events (no dragging) */
		MOVE_EVENTS,
		/** Sets state to deleting primitives */
		DELETE,
		/** exports current formula into an image */
		EXPORT_BITMAP,
		/** Writes current formula in LTL to screen */
		SHOW_FORMULA,
		/** Writes current formula in LTL to file */
		EXPORT_FORMULA,
		/**	Switches the visibility of time series */
		SWITCH_TS_VISIBILITY,
		/** Delete currently selected primitive */
		DELETE_PRIMITIVE;
	}
	private Map<ActionType,MutableAction> actions = new HashMap<ActionType, MutableAction>();
	
	/**
	 * Initializes actions and loads their names.
	 */
	public ActionManager() {
		ResourceBundle labels = ResourceBundle.getBundle("ui.labels");
		for (ActionType action : ActionType.values()) {
			//loads name of action from resource bundle and assigns ActionListener which does nothing
			actions.put(action, new MutableAction(labels.getString(action.name())));
		}
	}

	/**
	 * Sets action performed by given action type 
	 * 
	 * @param type Action type
	 * @param action ActionListener defining performed action
	 */
	public void setAction(ActionType type, ActionListener action) {
		actions.get(type).setActionListener(action);
	}
	
	/**
	 * Used to with <code>setAction</code> to configure a component (e.g. a <code>JButton</code>).
	 * 
	 * When action performed by the action type is changed (using {@link #setAction}), the change is
	 * reflected in functionality of <code>Action</code> assigned previously to any components.
	 * 
	 * @param type Action type
	 * @return {@link Action} object assigned to given action type
	 */
	public Action getAction(ActionType type) {
		return actions.get(type);
	}
	
}
