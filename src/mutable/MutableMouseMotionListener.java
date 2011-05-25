//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package mutable;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * {@link MouseMotionListener} placeholder with mutable actions.
 * Used for more comfortable MouseMotionListener management.
 * 
 * @author Tomáš Vejpustek
 */
public class MutableMouseMotionListener implements MouseMotionListener {
	/**
	 * For creating {@link MutableMouseMotionListener} from {@link MouseMotionListener}.
	 * 
	 * @author Tomáš Vejpustek
	 */
	private static abstract class ListenerMouseAction implements MouseAction {
		protected MouseMotionListener listener;
		public ListenerMouseAction(MouseMotionListener listener) {
			this.listener = listener;
		}		
	}
	private static MouseAction doNothing = new MouseAction() {
		@Override
		public void actionPerformed(MouseEvent e) {
		}
	};

	private MouseAction onMouseDragged = doNothing;
	private MouseAction onMouseMoved = doNothing;
	
	/**
	 * Creates {@link MutableMouseMotionListener} with no assigned actions.
	 */
	public MutableMouseMotionListener() {}
	
	/**
	 * Creates {@link MutableMouseMotionListener} with function specified by a {@link MouseMotionListener}.
	 * @param listener Desired functionality.
	 */
	public MutableMouseMotionListener(MouseMotionListener listener) {
		setListener(listener);
	}
	
	/**
	 * Assigns mouse actions from {@link MouseMotionListener}.
	 * @param listener Desired functionality.
	 */
	public void setListener(MouseMotionListener listener) {
		onMouseDragged = new ListenerMouseAction(listener) {
			@Override
			public void actionPerformed(MouseEvent e) {
				listener.mouseDragged(e);
			}
		};
		onMouseMoved = new ListenerMouseAction(listener) {
			@Override
			public void actionPerformed(MouseEvent e) {
				listener.mouseMoved(e);
			}
		};
	}
	
	/**
	 * @return Action invoked when mouse button is pressed on a component and then dragged.
	 */
	public MouseAction getMouseDragged() {
		return onMouseDragged;
	}
	
	/**
	 * Sets action invoked when mouse button is pressed on a component and then dragged. 
	 * @param onMouseDragged target action.
	 */
	public void setMouseDragged(MouseAction onMouseDragged) {
		this.onMouseDragged = onMouseDragged;
	}
	
	/**
	 * Removes action invoked when mouse button is pressed on a component and then dragged.
	 */
	public void unsetMouseDragged() {
		onMouseDragged = doNothing;
	}
	
	/**
	 * @return Action invoked when the mouse cursor has been moved onto a component (without buttons being pressed).
	 */
	public MouseAction getMouseMoved() {
		return onMouseMoved;
	}
	
	/**
	 * Sets action invoked when the mouse cursor has been moved onto a component (without buttons being pressed).
	 * @param onMouseMoved target action
	 */
	public void setMouseMoved(MouseAction onMouseMoved) {
		this.onMouseMoved = onMouseMoved;
	}
	
	/**
	 * Removes action invoked when the mouse cursor has been moved onto a component (without buttons being pressed).
	 */
	public void unsetMouseMoved() {
		onMouseMoved = doNothing;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		onMouseDragged.actionPerformed(e);
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		onMouseMoved.actionPerformed(e);
	}

}
