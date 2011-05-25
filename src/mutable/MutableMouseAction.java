//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package mutable;

import java.awt.event.MouseEvent;
/**
 * {@link MouseAction} with unfixed <code>actionPerformed</code>, which is defined by included {@link MouseAction}.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class MutableMouseAction implements MouseAction {
	private static MouseAction doNothing = new MouseAction() {
		@Override
		public void actionPerformed(MouseEvent e) {
		}
	};
	public MouseAction action;
	
	/**
	 * Action that does nothing.
	 */
	public MutableMouseAction() {
		action = doNothing;
	}
	
	/**
	 * Specifies performed action.
	 * @param action Specified action.
	 */
	public MutableMouseAction(MouseAction action) {
		this.action = action;
	}

	/**
	 * @return {@link MouseAction} specifying this action.
	 */
	public MouseAction getMouseAction() {
		return action;
	}
	
	/**
	 * Changes performed action.
	 * @param action Specifies new performed action.
	 */
	public void setMouseAction(MouseAction action) {
		this.action = action;
	}
	
	/**
	 * Changes performed action to no action.
	 */
	public void unsetMouseAction() {
		action = doNothing;
	}
	
	@Override
	public void actionPerformed(MouseEvent e) {
		action.actionPerformed(e);
	}

}
