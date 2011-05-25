//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package mutable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;

/**
 * Action with unfixed <code>actionPerformed</code>, which is defined by included <code>ActionListener</code>.
 * 
 * @author Tomáš Vejpustek
 *
 */
@SuppressWarnings("serial")
public class MutableAction extends AbstractAction {
	private static ActionListener doNothing = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		}
	};
	private ActionListener listener;

	/**
	 * Specifies only description -- action does nothing.
	 * @param name Action description.
	 */
	public MutableAction(String name) {
		super(name);
		listener = doNothing;
	}
	/**
	 * Specifies description and performed action
	 * 
	 * @param name Action description.
	 * @param listener Specifies performed action.
	 */
	public MutableAction(String name, ActionListener listener) {
		super(name);
		this.listener = listener;
	}
	
	/**
	 * Changes performed action.
	 * @param listener Specifies new performed action.
	 */
	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Sets performed action to no action.
	 */
	public void unsetActionListener() {
		listener = doNothing;
	}
	
	/**
	 * @return <code>ActionListener</code> specifying performed action.
	 */
	public ActionListener getActionListener() {
		return listener;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		listener.actionPerformed(e);
	}

}
