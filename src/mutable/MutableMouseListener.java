//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package mutable;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * {@link MouseListener} placeholder with mutable actions.
 * Used for more comfortable MouseListener management, when mouseactions 
 * 
 * @author Tomáš Vejpustek
 */
public class MutableMouseListener implements MouseListener {
	/**
	 * For creating {@link MutableMouseListener} from a {@link MouseListener}.
	 * 
	 * @author Tomáš Vejpustek
	 */
	private abstract static class ListenerMouseAction implements MouseAction {
		protected MouseListener listener;
		
		public ListenerMouseAction(MouseListener listener) {
			this.listener = listener;
		}
	}
	private static MouseAction doNothing = new MouseAction() {
		@Override
		public void actionPerformed(MouseEvent e) {
		}
	};
	
	private MouseAction onMouseClicked = doNothing;
	private MouseAction onMouseEntered = doNothing;
	private MouseAction onMouseExited = doNothing;
	private MouseAction onMousePressed = doNothing;
	private MouseAction onMouseReleased = doNothing;
	
	/**
	 * Creates {@link MutableMouseListener} with no assigned actions.
	 */
	public MutableMouseListener() {}
	
	/**
	 * Creates {@link MutableMouseListener} with same function as a listener.
	 * @param listener Desired functionality.
	 */
	public MutableMouseListener(MouseListener listener) {
		setListener(listener);
	}
	
	/**
	 * Assigns mouse actions from a {@link MouseListener}.
	 * @param listener Desired functionality.
	 */
	public void setListener(MouseListener listener) {
		onMouseClicked = new ListenerMouseAction(listener) {
			@Override
			public void actionPerformed(MouseEvent e) {
				listener.mouseClicked(e);
			}
		};
		onMouseEntered = new ListenerMouseAction(listener) {
			@Override
			public void actionPerformed(MouseEvent e) {
				listener.mouseEntered(e);
			}
		};
		onMouseExited = new ListenerMouseAction(listener) {
			@Override
			public void actionPerformed(MouseEvent e) {
				listener.mouseExited(e);
			}
		};
		onMousePressed = new ListenerMouseAction(listener) {
			@Override
			public void actionPerformed(MouseEvent e) {
				listener.mousePressed(e);
			}
		};
		onMouseReleased = new ListenerMouseAction(listener) {
			@Override
			public void actionPerformed(MouseEvent e) {
				listener.mouseReleased(e);
			}
		};
	}
		
	/**
	 * @return Action invoked when mouse button is clicked on a component.
	 */
	public MouseAction getMouseClicked() {
		return onMouseClicked;
	}

	/**
	 * Sets action invoked when mouse button is clicked on a component.
	 * @param onMouseClicked target mouse action.
	 */
	public void setMouseClicked(MouseAction onMouseClicked) {
		this.onMouseClicked = onMouseClicked;
	}

	/**
	 * Removes action invoked when the mouse button is clicked on a component.
	 */
	public void unsetMouseClicked() {
		onMouseClicked = doNothing;
	}

	
	/**
	 * @return Action invoked when mouse enters a component.
	 */
	public MouseAction getMouseEntered() {
		return onMouseEntered;
	}

	/**
	 * Sets action invoked when mouse enters a component.
	 * @param onMouseEntered target action.
	 */
	public void setMouseEntered(MouseAction onMouseEntered) {
		this.onMouseEntered = onMouseEntered;
	}

	/**
	 * Removes action invoked when mouse enters a component.
	 */
	public void unsetMouseEntered() {
		onMouseEntered = doNothing;
	}

	
	/**
	 * @return Action invoked when mouse exits a component.
	 */
	public MouseAction getMouseExited() {
		return onMouseExited;
	}

	/**
	 * Sets action invoked when mouse exits a component. 
	 * @param onMouseExited target action.
	 */
	public void setMouseExited(MouseAction onMouseExited) {
		this.onMouseExited = onMouseExited;
	}

	/**
	 * Removes action invoked when mouse exits a component.
	 */
	public void unsetMouseExited() {
		onMouseExited = doNothing;
	}
	

	/**
	 * @return Action invoked when mouse is pressed on a component.
	 */
	public MouseAction getMousePressed() {
		return onMousePressed;
	}

	/**
	 * Sets action invoked when mouse is pressed on a component.
	 * @param onMousePressed
	 */
	public void setMousePressed(MouseAction onMousePressed) {
		this.onMousePressed = onMousePressed;
	}

	/**
	 * Removes action invoked when mouse is pressed on a component.
	 */
	public void unsetMousePressed() {
		onMousePressed = doNothing;
	}

	/**
	 * @return Action invoked when mouse is released on a component.
	 */
	public MouseAction getMouseReleased() {
		return onMouseReleased;
	}

	/**
	 * Sets action invoked when mouse is released on a component.
	 * @param onMouseReleased target action
	 */
	public void setMouseReleased(MouseAction onMouseReleased) {
		this.onMouseReleased = onMouseReleased;
	}
	
	/**
	 * Removes action invoked when mouse is released on a component.
	 */
	public void unsetMouseReleased() {
		onMouseReleased = doNothing;
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		onMouseClicked.actionPerformed(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		onMouseEntered.actionPerformed(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		onMouseExited.actionPerformed(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		onMousePressed.actionPerformed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		onMouseReleased.actionPerformed(e);
	}

}
