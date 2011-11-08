//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import selector.EventSelector;
import selector.Selector;
import selector.TransitionSelector;
import xml.XMLRepresentable;
import coordinates.Transformation;
import exceptions.XMLException;

/**
 * Model of graphic formula constituting of {@link Event}s and {@link Transition}.
 * 
 * Defines model manipulation methods. These are used by {@link ModelChange} objects.
 * To modify model, use {@link #applyChange(ModelChange)}.
 * 
 * Stores information about selected graphic primitive.
 *  
 * @author Tomáš Vejpustek
 *
 */
public class Model implements XMLRepresentable, LTLRepresentable {
	private List<Event> events = new ArrayList<Event>();
	private List<Transition> transitions = new ArrayList<Transition>();
	private int selectedEvent = -1;
	private int selectedTransition = -1;
	
	/**
	 * Creates empty model with default transition (initial and terminal).
	 */
	public Model() {
		transitions.add(new Transition(null, null));
	}
	
	/**
	 * Removes all events and transitions from the model and creates default transition.
	 */
	public void clear() {
		unselect();
		events.clear();
		transitions.clear();
		transitions.add(new Transition(null, null));
	}

	/**
	 * Inserts <code>event</code> into this model.
	 * @return Index of inserted event.
	 */
	public EventLocation addEvent(Event event) {
		if (isEventSelected() || isTransitionSelected()) {
			throw new IllegalStateException("Cannot add events when model is being edited.");
		}
		
		Comparator<Event> comp = new EventTimeComparator();
		int index = 0;
		while ((index < events.size()) && (comp.compare(event, events.get(index)) >= 0)) {
			index++;
		}
		events.add(index, event);
		Transition removed = transitions.remove(index);
		Transition [] inserted = removed.split(event);
		transitions.add(index, inserted[0]);
		transitions.add(index+1, inserted[1]);
		return new EventLocation(index, inserted[0], inserted[1], removed);
	}
	
	/**
	 * Insert event to given index and changes its adjacent transitions. Includes no control; used by {@link CreateEvent} and {@link DeleteEvent}.
	 * @param target Target event.
	 * @param index Index of target event.
	 * @param left Transition adjacent to the event from left.
	 * @param right Transition adjacent to the event from right.
	 */
	void insertEvent(Event target, int index, Transition left, Transition right) {
		events.add(index, target);
		transitions.remove(index);
		transitions.add(index, left);
		transitions.add(index+1, right);
	}
	
	/**
	 * Deletes selected event
	 * @return Index of deleted event.
	 */
	public EventLocation deleteEvent() {
		if (!isEventSelected()) {
			throw new IllegalStateException("Cannot delete event when there is no selected.");
		}
		events.remove(selectedEvent);
		Transition left = transitions.remove(selectedEvent);
		Transition right = transitions.remove(selectedEvent);
		Transition joined = left.righJoin(right);
		EventLocation result = new EventLocation(selectedEvent, left, right, joined);
		transitions.add(selectedEvent, joined);
		selectedEvent = -1;
		return result;
	}

	/**
	 * Removes event from given index and changes its adjacent trasitions. Includes no control; used by {@link CreateEvent} and {@link DeleteEvent}.
	 * @param index Index of target event.
	 * @param joined Transition to replace the place of target event and its adjacent transitions.
	 */
	void removeEvent(int index, Transition joined) {
		events.remove(index);
		transitions.remove(index);
		transitions.remove(index);
		transitions.add(index, joined);
	}
	
	/**
	 * Inserts <code>event</code> in the place of selected event. 
	 * @return Index of modified event.
	 * @throws IllegalArgumentException when the new event does not fit the space.
	 */
	public int modifyEvent(Event event) {
		if (!isEventSelected()) {
			throw new IllegalStateException("Cannot modify event when no event is selected.");
		}
		Comparator<Event> comp = new EventTimeComparator();
		if (((selectedEvent > 0) && (comp.compare(event, events.get(selectedEvent - 1)) < 0))
				|| ((selectedEvent < events.size() - 1) && (comp.compare(event, events.get(selectedEvent + 1)) >= 0))) {
			throw new IllegalArgumentException("Modified event does not fit the space of selected event.");
		}
		
		int index = selectedEvent;
		events.remove(index);
		events.add(index, event);
		transitions.get(index).setRight(event); //needed for undo and redo
		transitions.get(index+1).setLeft(event); //needed for undo and redo
		selectedEvent = -1;
		return index;
	}
	
	/**
	 * Select event and modify it.
	 * @param target Event to replace modified event.
	 * @param index Index of event to be modified.
	 * @see #modifyEvent(Event)
	 */
	void modifyEvent(Event target, int index) {
		if ((index >= events.size()) || (index < 0)) {
			throw new IllegalArgumentException("Invalid event index.");
		}
		if (isEventSelected()) {
			throw new IllegalStateException("Event already selected.");
		}
		selectedEvent = index;
		modifyEvent(target);
	}

	/**
	 * Inserts <code>transition</code> in the place of selected transition.
	 * @return Index of modified transition.
	 * @throws IllegalArgumentException when the new transition does not have equivalent adjacent events.
	 */
	public int modifyTransition(Transition transition) {
		if (!isTransitionSelected()) {
			throw new IllegalStateException("Cannot modify transition when no transition is selected.");
		}
		Transition original = transitions.get(selectedTransition);
		if (!(((original.getLeft() == null && transition.getLeft() == null) || original.getLeft().equals(transition.getLeft()))
			&& ((original.getRight() == null && transition.getRight() == null) || original.getRight().equals(transition.getRight())))) {
			throw new IllegalArgumentException("Modified transition does not have equivalent adjacent events.");
		}
		int index = selectedTransition;
		transitions.remove(index);
		transitions.add(index, transition);
		selectedTransition = -1;
		return index;
	}
	
	/**
	 * Select transition and modify it.
	 * @param target Transition to replace modified transition.
	 * @param index Index of transition to be modified.
	 * @see #modifyTransition(Transition)
	 */
	void modifyTransition(Transition target, int index) {
		if ((index < 0) || (index >= transitions.size())) {
			throw new IllegalArgumentException("Invalid transition index.");
		}
		if (isTransitionSelected()) {
			throw new IllegalStateException("Transition already selected.");
		}
		selectedTransition = index;
		modifyTransition(target);
	}
	
	/**
	 * Unselects any previously selected selected graphical primitive.
	 */
	public void unselect() {
		selectedEvent = -1;
		selectedTransition = -1;
	}
	
	/**
	 * @param coord coordinate transformation.
	 * @return Event selector for event of particular index.
	 */
	public EventSelector getEventSelector(int index, Transformation coord) {
		if (isEventSelected() || isTransitionSelected()) {
			throw new IllegalStateException("Event or Transition is already selected.");
		}
		selectedEvent = index;
		return EventSelector.get(coord, events.get(index), transitions.get(index), transitions.get(index+1));
	}

	/**
	 * @param coord coordinate transformation.
	 * @return Transition selector for transition of particular index. 
	 */
	public TransitionSelector getTransitionSelector(int index, Transformation coord) {
		if (isEventSelected() || isTransitionSelected()) {
			throw new IllegalStateException("An Event or Transition is selected, cannot select another.");
		}
		selectedTransition = index;
		return TransitionSelector.get(coord, transitions.get(index));
	}
	
	/**
	 * @param coord coordinate transformation.
	 * @param p point in on-screen coordinates.
	 * @return List of events which contain target point.
	 */
	private List<Event> getSelectedEvents(Point2D p, Transformation coord) {
		List<Event> selected = new ArrayList<Event>();
		for (Event e : events) {
			if (e.contains(p, coord)) {
				selected.add(e);
			}
		}
		return selected;
	}
	
	/**
	 * 
	 * @param p point in on-screen coordinates.
	 * @param coord coordinate transformation.
	 * @return Selector for graphic primitive selected by clicking target point.
	 */
	public Selector getSelected(Point2D p, Transformation coord) {
		if (isEventSelected() || isTransitionSelected()) {
			throw new IllegalStateException("An Event or Transformation is selected, cannot select another.");
		}
		List<Event> selEvents = getSelectedEvents(p, coord);
		if (!selEvents.isEmpty()) {
			int minPriority = 10; //sufficiently high number
			Event priorityEvent = null;
			for (Event e : selEvents) {
				if (e.selectionPriority() < minPriority) {
					minPriority = e.selectionPriority();
					priorityEvent = e;
				}
			}
			selectedEvent = events.indexOf(priorityEvent);
			if (selectedEvent >= 0) {
				return EventSelector.get(coord, priorityEvent, transitions.get(selectedEvent), transitions.get(selectedEvent+1));
			}
		}
		for (int index = 0; index < transitions.size(); index++) {
			if (transitions.get(index).contains(p, coord)) {
				selectedTransition = index;
				return TransitionSelector.get(coord, transitions.get(index)); 
			}
		}
		return null;
	}

	/**
	 * @return Iterator of contained events.
	 */
	public Iterator<Event> getEventsIterator() {
		return new SkipReader<Event>(events, selectedEvent);
	}
	
	/**
	 * @return Iterator of contained transitions.
	 */
	public Iterator<Transition> getTransitionsIterator() {
		return new SkipReader<Transition>(transitions, selectedTransition);
	}
	
	/**
	 * @return <code>true</code> if a contained event is selected, <code>false</code> otherwise.
	 */
	private boolean isEventSelected() {
		return (selectedEvent >= 0);
	}
	
	/**
	 * @return <code>true</code> if a contained event is selected, <code>false</code> otherwise.
	 */
	private boolean isTransitionSelected() {
		return (selectedTransition >= 0);
	}
	
	@Override
	public void loadFromXML(Node node) throws XMLException {
		clear();
		NodeList nodes = node.getChildNodes();
		
		Map<Integer, Event> events = new HashMap<Integer, Event>();
		for (int index = 0; index < nodes.getLength(); index++) {
			Node n = nodes.item(index);
			if (n.getNodeName().equals("event")) {
				Event e = new Event(1, 1);
				e.loadFromXML(n);
				Integer id = Integer.valueOf(((Element)n).getAttribute("id"));
				if (events.put(id, e) != null) {
					throw new XMLException("err_xml_id_duplicity", "Duplicate Event ids in input file.");
				}
				addEvent(e);
			}
		}
		
		for (int index = 0; index < nodes.getLength(); index++) {
			Node n = nodes.item(index);
			if (n.getNodeName().equals("transition")) {
				Element e = (Element)n;
				Event left, right;
				if (e.hasAttribute("left")) {
					left = events.get(Integer.valueOf(e.getAttribute("left")));
					if (left == null) {
						throw new XMLException("err_xml_id_left", "Transition with left event which does not exist.");
					}
				} else {
					left = null;
				}
				if (e.hasAttribute("right")) {
					right = events.get(Integer.valueOf(e.getAttribute("right")));
					if (right == null) {
						throw new XMLException("err_xml_id_right", "Transition with right event which does not exist.");
					}
				} else {
					right = null;
				}
				Transition t = new Transition(left, right);
				t.loadFromXML(n);
				
				int leftId = (t.getLeft() == null) ? -1 : this.events.indexOf(t.getLeft());
				if (((leftId == this.events.size() - 1) && t.getRight() == null)
						|| t.getRight().equals(this.events.get(leftId+1))) {
					if (!transitions.remove(leftId+1).isEmpty()) {
						throw new XMLException("err_xml_transition_duplicate", "Duplicate transition.");
					}
					transitions.add(leftId+1, t);
				} else if (leftId < ((t.getRight() == null) ? this.events.size() : this.events.indexOf(t.getRight()))) {
					throw new XMLException("xml_transitions_general", "General transitions not yet supported.");
				} else {
					throw new XMLException("err_xml_transitions_inconsistent", "Inconsistent ids in input file.");
				}
			}
		}
	}

	@Override
	public Node toXML(Document document) {
		return toXML(document, "formula");
	}

	@Override
	public Node toXML(Document document, String name) {
		Element model = document.createElement(name);
		for (int index = 0; index < events.size(); index++) {
			Element e = (Element)events.get(index).toXML(document);
			e.setAttribute("id", Integer.toString(index));
			model.appendChild(e);
		}
		//default transitions
		for (int index = 0; index < transitions.size(); index++) {
			if (!transitions.get(index).isEmpty()) {
				Element e = (Element)transitions.get(index).toXML(document);
				if (index != 0) {
					e.setAttribute("left", Integer.toString(index-1));
				}
				if (index != transitions.size() - 1) {
					e.setAttribute("right", Integer.toString(index));
				}
				model.appendChild(e);
			}
		}
		
		//general transitions
		/*Iterator<Transition> trans = transitions.iterator();
		while (trans.hasNext()) {
			Transition t = trans.next();
			if (!t.isEmpty()) {
				Element e = (Element)t.toXML(document);
				if (t.getLeft() != null) {
					e.setAttribute("left", Integer.toString(events.indexOf(t.getLeft())));
				}
				if (t.getRight() != null) {
					e.setAttribute("right", Integer.toString(events.indexOf(t.getRight())));
				}
			}
		}*/
		return model;
	}
	
	@Override
	public String toLTL(FormulaBuilder builder) {
		String formula = "";
		Transition last = transitions.get(transitions.size() - 1);
		if (!last.isEmpty()) {
			formula = builder.globally(last.toLTL(builder));
		}
		for (int index = events.size() - 1; index >= 0; index--) {
			Event event = events.get(index);
			String eventForm = event.toLTL(builder);
			if (!event.isEmpty()) {
				if (formula.isEmpty()) {
					formula = eventForm;
				} else {
					formula = builder.and(eventForm, builder.next(formula));
				}
			}
			
			Transition trans = transitions.get(index);
			String transForm = trans.toLTL(builder);
			
			if (!trans.isEmpty()) {
				if (formula.isEmpty()) {
					formula = builder.globally(transForm);
				} else {
					formula = builder.until(transForm, formula);
				}
			} else {
				if (!formula.isEmpty()) {
					formula = builder.future(formula);
				}
			}
		}
		return formula;
	}

}
