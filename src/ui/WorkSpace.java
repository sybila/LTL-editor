//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ui;

import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import ltl.Event;
import ltl.FormulaBuilder;
import ltl.Model;
import ltl.ModelChange;
import ltl.Transition;
import ltl.TransitionCyclicProperty;
import ltl.TransitionPositiveProperty;
import mutable.MouseAction;
import mutable.MutableMouseListener;
import mutable.MutableMouseMotionListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import selector.EventCreator;
import selector.Selector;
import series.TimeSeries;
import ui.MouseActionManager.MouseActionType;
import xml.XMLException;
import coordinates.Transformation;

/**
 * Place in which the time series is displayed and annotated by the user.
 * 
 * @author Tomáš Vejpustek
 *
 */
@SuppressWarnings("serial")
public class WorkSpace extends JPanel implements ComponentListener, MouseMotionListener, MouseListener {

	private Main parent;
	private StatusBar statusBar;
	private Transformation coord;
	private TimeSeries series = new TimeSeries(); //empty time series
	private Model model = new Model();
	
	private boolean timeSeriesVisible = true;
	
	private Selector active = null;
	private boolean selectorsActive = true;
	
	private MutableMouseListener mouseListener = new MutableMouseListener();
	private MutableMouseMotionListener mouseMotionListener = new MutableMouseMotionListener();
	private MouseActionManager actions = new MouseActionManager();

	/**
	 * Create the panel.
	 * @param parent frame to notify whether a primitive is selected.
	 * @param statusBar display of status information 
	 */
	public WorkSpace(Main parent, StatusBar statusBar) {
		this.parent = parent;
		this.statusBar = statusBar;
		addComponentListener(this);
		addMouseListener(mouseListener);
		addMouseListener(this);
		addMouseMotionListener(mouseMotionListener);
		addMouseMotionListener(this);
		
		actions.setMouseAction(MouseActionType.CREATE_EVENT_PRESS, new MouseAction() {
			@Override
			public void actionPerformed(MouseEvent e) {
				active = new EventCreator(coord, new Point2D.Double(e.getX(), e.getY()));
				refresh();
			}
		});
		actions.setMouseAction(MouseActionType.EDIT_MODEL_PRESS, new MouseAction() {
			@Override
			public void actionPerformed(MouseEvent e) {
				if (active != null) {
					Point2D p = new Point2D.Double(e.getX(), e.getY());
					if (active.contains(p)) {
						active.startDrag(e);
						refresh();
					} else if (active.objectContains(p)) {
						active.startMove(p);
						refresh();
					} else {
						model.unselect();
						active = null;
					}
					getParentForm().setPrimitiveSelected(false);
				}
			}
		});
		actions.setMouseAction(MouseActionType.SELECT_PRIMITIVE, new MouseAction() {
			@Override
			public void actionPerformed(MouseEvent e) {
				Point2D p = new Point2D.Double(e.getX(), e.getY());
				if (active == null || (!active.contains(p) && !active.objectContains(p))) {
					Selector selected = model.getSelected(p, coord);
					if (selected != null) {
						active = selected;
						getParentForm().setPrimitiveSelected(true);
					}
					refresh();
				}
			}
		});
		actions.setMouseAction(MouseActionType.MOVE_EVENT_PRESS, new MouseAction() {			
			@Override
			public void actionPerformed(MouseEvent e) {
				if (active != null) {
					Point2D p = new Point2D.Double(e.getX(), e.getY());
					if (active.contains(p) || active.objectContains(p)) {
						active.startMove(p);
						refresh();
					} else {
						model.unselect();
						active = null;
					}
					getParentForm().setPrimitiveSelected(false);
				}
				
			}
		});
		actions.setMouseAction(MouseActionType.SELECTOR_DRAG, new MouseAction() {
			@Override
			public void actionPerformed(MouseEvent e) {
				if (active != null) {
					if (active.isDragging()) {
						active.drag(e);
					} else if (active.isMoving()) {
						active.move(new Point2D.Double(e.getX(), e.getY()));
					}
					refresh();
				}
			}
		});
		actions.setMouseAction(MouseActionType.SELECTOR_RELEASED, new MouseAction() {
			@Override
			public void actionPerformed(MouseEvent e) {
				if (active != null) {
					if (active.isDragging() || active.isMoving()) {
						ModelChange change;
						if (active.isDragging()) {
							change = active.endDrag(e);
						} else {
							change = active.endMove(new Point2D.Double(e.getX(), e.getY()));
						}
						model.applyChange(change);
						active = change.selector(model, coord);
						if (active != null) {
							getParentForm().setPrimitiveSelected(true);
						}
					}
					refresh();
				}
			}
		});
		actions.setMouseAction(MouseActionType.DELETE_PRIMITIVE, new MouseAction() {
			@Override
			public void actionPerformed(MouseEvent e) {
				Selector selected = model.getSelected(new Point2D.Double(e.getX(), e.getY()), coord);
				if (selected != null) {
					model.applyChange(selected.delete());
					refresh();
				}
			}
		});
		modeCreateEvents();
	}

	@Override
	public void paint(Graphics g) {
		Canvas canvas = new Canvas(g, coord, getWidth(), getHeight(), selectorsActive);
		
		if (timeSeriesVisible) {
			canvas.drawTimeSeries(series);
		}
		
		Iterator<Transition> transitions = model.getTransitionsIterator();
		while (transitions.hasNext()) {
			Transition newTrans = transitions.next();
			canvas.drawTransition(newTrans);
		}
		
		Iterator<Event> events = model.getEventsIterator();
		while (events.hasNext()) {
			Event newEvent = events.next();
			canvas.drawEvent(newEvent);
		}
		
		if (active != null) {
			active.draw(canvas);
		}
	}
	
	/**
	 * Changes displayed time series.
	 */
	public void setTimeSeries(TimeSeries series) {
		unselect();
		
		if (series != null) {
			coord.setLinearTransformation(series);
			this.series = series;
		} else {
			coord.setIdentity();
			this.series = new TimeSeries();
		}
		TransitionPositiveProperty.setEnd(coord.getConcentrationBound()/2);
		TransitionCyclicProperty.setTimeEnd(coord.getTimeBound());
		TransitionCyclicProperty.setConcentrationEnd(coord.getConcentrationBound()/2);
		timeSeriesVisible = true;
		refresh();
	}
	
	/**
	 * Writes model as an XML structure into output stream (e.g. a file).
	 */
	public void writeModel(OutputStream os) throws XMLException{
		unselect();
		DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuild;
		try {
			docBuild = docFac.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			throw new XMLException("err_xml_general", "Parser could not be configured.");
		}
		
		Document doc = docBuild.newDocument();
		Element mod = (Element)model.toXML(doc);
		mod.setAttribute("xmlns", "http://www.fi.muni.cz/~xvejpust/TimeSeriesLTLAnnotator"); //set name space
		doc.appendChild(mod);
		
		TransformerFactory transFac = TransformerFactory.newInstance();
		Transformer trans;
		try {
			trans = transFac.newTransformer();
		} catch (TransformerConfigurationException tce) {
			throw new XMLException("err_xml_general", "Transformer could not be configured.");
		}
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		try {
			trans.transform(new DOMSource(doc), new StreamResult(os));
		} catch (TransformerException te) {
			throw new XMLException("unknown", "XML Transformer could not write to the file.", te);
		}
	}

	/**
	 * Loads model from an XML structure in an input stream (e.g. a file).  
	 */
	public void loadModel(InputStream is) throws XMLException{
		unselect();
		SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema;
		try {
			schema = sf.newSchema(getClass().getResource("formula.xsd"));
		} catch (SAXException saxe) { //schema cannot be loaded
			throw new XMLException("err_xml_schema", "Document schema could not be read.");
		}
		Validator valid = schema.newValidator();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			throw new XMLException("err_xml_general", "Parser could not be configured.");
		}
		Document doc;
		try {
			doc = builder.parse(is);
		} catch (SAXException saxe) { //not an XML
			throw new XMLException("err_xml_parse", "Document parse error occurred.", saxe);
		} catch (IOException ioe) {
			throw new XMLException("err_input", "An IO error has occurred during document parsing.", ioe);
		}
		DOMResult result = new DOMResult();
		try {
			valid.validate(new DOMSource(doc), result);
		} catch (SAXException saxe) {
			throw new XMLException("err_xml_validation", "Document validation error occurred.", saxe);
		} catch (IOException ioe) {
			throw new XMLException("err_input", "An IO error has occurred during document validation.", ioe);
		}
		Document input = (Document)result.getNode();
		input.normalize();
		
		Model loaded = new Model();
		loaded.loadFromXML(input.getDocumentElement());
		model = loaded;
		refresh();
	}
	
	/**
	 * @return LTL formula specified in this workspace.
	 */
	public String getFormula() {
		unselect();
		return model.toLTL(new FormulaBuilder());
	}
	
	/**
	 * Switches to mode where events are created on mouse events.
	 */
	public void modeCreateEvents() {
		unselect();
		mouseListener.setMousePressed(actions.getMouseAction(MouseActionType.CREATE_EVENT_PRESS));
		mouseListener.setMouseReleased(actions.getMouseAction(MouseActionType.SELECTOR_RELEASED));
		mouseListener.unsetMouseClicked();
		mouseMotionListener.setMouseDragged(actions.getMouseAction(MouseActionType.SELECTOR_DRAG));
	}
	
	/**
	 * Switches to mode where graphical primitives may be edited and moved.
	 */
	public void modeEditModel() {
		mouseListener.setMousePressed(actions.getMouseAction(MouseActionType.EDIT_MODEL_PRESS));
		mouseListener.setMouseClicked(actions.getMouseAction(MouseActionType.SELECT_PRIMITIVE));
		mouseListener.setMouseReleased(actions.getMouseAction(MouseActionType.SELECTOR_RELEASED));
		mouseMotionListener.setMouseDragged(actions.getMouseAction(MouseActionType.SELECTOR_DRAG));
		selectorsActive = true;
		refresh();
	}
	
	/**
	 * Switches to mode where Events may be moved (added to respect "small" events").
	 */
	public void modeMoveEvents() {
		mouseListener.setMousePressed(actions.getMouseAction(MouseActionType.MOVE_EVENT_PRESS));
		mouseListener.setMouseClicked(actions.getMouseAction(MouseActionType.SELECT_PRIMITIVE));
		mouseListener.setMouseReleased(actions.getMouseAction(MouseActionType.SELECTOR_RELEASED));
		mouseMotionListener.setMouseDragged(actions.getMouseAction(MouseActionType.SELECTOR_DRAG));
		selectorsActive = false;
		refresh();
	}
	
	/**
	 * Switches to mode where graphical primitives may be deleted.
	 */
	public void modeDelete() {
		unselect();
		mouseListener.unsetMousePressed();
		mouseListener.unsetMouseReleased();
		mouseListener.setMouseClicked(actions.getMouseAction(MouseActionType.DELETE_PRIMITIVE));
		mouseMotionListener.unsetMouseDragged();
	}
	
	/**
	 * Deletes currently selected primitive.
	 */
	public void deleteSelected() {
		if (active == null) {
			throw new IllegalStateException("Cannot delete selected primitive when none is selected.");
		}
		model.applyChange(active.delete());
		active = null;
		refresh();
	}
	
	/**
	 * Unselects current selected primitive.
	 */
	public void unselect() {
		if (active != null) {
			if (active.isDragging() || active.isMoving()) {
				throw new IllegalStateException("Cannot unselected graphical primitive that is being edited.");
			}
			model.unselect();
			active = null;
			getParentForm().setPrimitiveSelected(false);
			refresh();
		}
	}
	
	/**
	 * Deletes all graphical primitives of model.
	 */
	public void clearModel() {
		model.clear();
		active = null;
	}

	/**
	 * Repaints the workspace and updates its status bar.
	 */
	public void refresh() {
		if (active != null) {
			active.refreshStatusBar(statusBar);
		} else {
			statusBar.clearSelected();
		}
		repaint();
	}
	
	/**
	 * Refreshes coordinates displayed by the status bar.
	 * @param e {@link MouseEvent} specifying displayed coordinates.
	 */
	private void refreshStatusCoordinates(MouseEvent e) {
		statusBar.setCoordinates(new Point2D.Double(coord.getTime(e.getX()), coord.getConcentration(e.getY())));
	}
	
	/**
	 * Switches time series from visible to invisible and vice-versa
	 * @return Status of time series visibility: <code>true</code> if it is visible, <code>false</code> otherwise.
	 */
	public boolean switchTimeSeriesVisible() {
		timeSeriesVisible = !timeSeriesVisible;
		refresh();
		return timeSeriesVisible;
	}
	
	private Main getParentForm() {
		return parent;
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (coord == null) {
			coord = new Transformation(getWidth(), getHeight());
		} else {
			coord.resize(getWidth(), getHeight());
		}
		TransitionPositiveProperty.setEnd(coord.getConcentrationBound()/2);
		TransitionCyclicProperty.setTimeEnd(coord.getTimeBound());
		TransitionCyclicProperty.setConcentrationEnd(coord.getConcentrationBound()/2);
		repaint();
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		refreshStatusCoordinates(e);
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		refreshStatusCoordinates(e);
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		refreshStatusCoordinates(e);
	}
	@Override
	public void mouseExited(MouseEvent e) {
		statusBar.clearCoordinates();
	}
	@Override
	public void componentShown(ComponentEvent e) {}
	@Override
	public void componentHidden(ComponentEvent e) {}
	@Override
	public void componentMoved(ComponentEvent e) {}
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}

}
