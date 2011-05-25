//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package selector;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import ltl.ModelChange;
import ui.Canvas;
import ui.StatusBar;

/**
 * Interface for manipulating graphical primitives. Generally comprises active points (aka green squares).
 * When active components are dragged, selected primitive is transformed, when area of primitive but not
 * active points are dragged, primitive is moved.
 * 
 * Created in the moment of selecting a primitive, destroyed when unselecting it. Significant change
 * of primitive may cause it to be reloaded (i.e. is reloaded after each {@link #endDrag(MouseEvent)}).
 * 
 * Manipulates with on-screen coordinates.
 * 
 * When a primitive is selected, its drawing is managed by the selector.
 * 
 * @author Tomáš Vejpustek
 *
 */
public interface Selector {
	
	/**
	 * @return <code>true</code> when <code>p</code> is inside the selector's active points, <code>false</code> otherwise.
	 */
	public boolean contains(Point2D p);
	
	/**
	 * @return <code>true</code> when <code>p</code> is inside selected graphical primitive
	 * (with no respect to selector's active points), <code>false</code> otherwise.
	 */
	public boolean objectContains(Point2D p);
	
	/**
	 * Draws the selector including selected primitive. 
	 * @param c Place for drawing.
	 */
	public void draw (Canvas c);
	
	
	/**
	 * @return <code>true</code> when selector is performing moving operation, <code>false</code> otherwise.
	 */
	public boolean isMoving();
	
	/**
	 * Starts moving operation from <code>p</code>.
	 * @return <code>true</code> if move operation was started, <code>false</code> otherwise (i.e. moving outside of primitive, ...)
	 * @throws IllegalStateException when already moving or dragging.
	 */
	public boolean startMove(Point2D p);
	
	/**
	 * Continues moving operation in <code>p</code>.
	 * @throws IllegalStateException when not moving. 
	 */
	public void move(Point2D p);
	
	/**
	 * Terminates moving operation in <code>p</code>.
	 * @return Model change representing the performed operation.
	 * @throws IllegalStateException when not moving.
	 */
	public ModelChange endMove(Point2D p);
	
	
	/**
	 * @return <code>true</code> when selector is performing dragging operation, <code>false</code> otherwise.
	 */
	public boolean isDragging();
	
	/**
	 * Starts dragging operation from given point.
	 * @param e {@link MouseEvent} specifying starting point.
	 * @return <code>true</code> if drag operation was started, <code>false</code> otherwise (i.e. dragging outside of active point, ...)
	 * @throws IllegalStateException when already dragging or moving.
	 */
	public boolean startDrag(MouseEvent e);
	
	/**
	 * Continues dragging operation in given point.
	 * @param e {@link MouseEvent} specifying point.
	 * @throws IllegalStateException when not dragging.
	 */
	public void drag(MouseEvent e);
	
	/**
	 * Terminates dragging operation in given point.
	 * @param e {@link MouseEvent} specifying point.
	 * @return Model change representing the performed operation.
	 * @throws IllegalStateException when not dragging.
	 */
	public ModelChange endDrag(MouseEvent e);
	
	/**
	 * Deletes selected graphical primitive.
	 * @return Model change representing the removal of primitive.
	 * @throws IllegalStateException when dragging or moving.
	 */
	public ModelChange delete();
	
	/**
	 * Refreshes information about selected primitive showed by <code>target</code>.
	 */
	public void refreshStatusBar(StatusBar target);
}
