//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.
package ltl;


/**
 * 
 * Contains information about an {@link Event} location in a {@link Model} and about its neighbourhood.
 * 
 * In practice, only a wrapper for a tuple.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class EventLocation {
	private int index;
	private Transition left, right, joined;
	
	/**
	 * Initializes all information.
	 * @param index Index of the event.
	 * @param left Transition to the left of event.
	 * @param right Transition to the right of event.
	 * @param joined Transition which would take the place of the event if it was not there.
	 */
	public EventLocation(int index, Transition left, Transition right, Transition joined) {
		this.index = index;
		this.left = left;
		this.right = right;
		this.joined = joined;
	}

	/**
	 * @return Index of the event.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return Transition to the left of the event.
	 */
	public Transition getLeft() {
		return left;
	}

	/**
	 * @return Transition to the left of the event.
	 */
	public Transition getRight() {
		return right;
	}

	/**
	 * @return Transition that would take place of the event if it was not there.
	 */
	public Transition getJoined() {
		return joined;
	}

}
