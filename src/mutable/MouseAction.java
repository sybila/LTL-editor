//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package mutable;

import java.awt.event.MouseEvent;

/**
 * Wrapper for any mouse action (such as mouseClicked, ...). Used to store action, which may
 * be assigned to several different mouse actions.
 * 
 * @author Tomáš Vejpustek
 *
 */
public interface MouseAction {

	/**
	 * Invoked when mouse action is invoked.
	 * @param e Cause of invoking.
	 */
	public void actionPerformed(MouseEvent e);
}
