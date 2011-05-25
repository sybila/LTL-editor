//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

import java.util.Iterator;
import java.util.List;

/**
 * Iterator of a {@link List} which skips certain element (given by its index). When skipped elements has an index
 * that is out of bound of iterated {@link List}, no element is skipped.
 * 
 * Function {@link #remove()} is not supported.
 * 
 * @author Tomáš Vejpustek
 */
public class SkipReader<E> implements Iterator<E> {
	private List<E> elements;
	private int selected;
	private int index = -1;

	/**
	 * Creates iterator of <code>elements</code> which skips no element.
	 */
	public SkipReader(List<E> elements) {
		this.elements = elements;
		selected = -1;
	}
	
	/**
	 * General constructor.
	 * @param elements Iterated list.
	 * @param skipped Index of skipped element. 
	 */
	public SkipReader(List<E> elements, int skipped) {
		this.elements = elements;
		selected = skipped;
	}
	
	@Override
	public boolean hasNext() {
		if ((index + 1 == selected) && (selected == elements.size() - 1)) {
			return false;
		}
		return (index + 1 < elements.size());
	}

	@Override
	public E next() {
		index++;
		if (index == selected) {
			index++;
		}
		return elements.get(index);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("SkipReader cannot remove from iterated list.");

	}
}
