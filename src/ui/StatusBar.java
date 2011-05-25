//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import ltl.Event;
import ltl.Property;
import ltl.Transition;
import ltl.Property.Bound;
import ui.ActionManager.ActionType;

/**
 * Status bar displays information about current position of cursor in model coordinates and
 * selected graphic primitive.
 * 
 * Also contains switch of time series visibility.
 * 
 * @author Tomáš Vejpustek
 */
@SuppressWarnings("serial")
public class StatusBar extends JPanel implements ComponentListener {
	private static final int MAXIMUM_HEIGHT = 20;
	private static final Border LABEL_BORDER = new BevelBorder(BevelBorder.LOWERED);
	private static final Font LABEL_FONT = new Font("Dialog", Font.PLAIN, 12);
	
	private static final String EQUALS = "=";
	private static final String LESSER = "<";
	
	private String time = "t";
	private String conc = "[X]";
	private String der = "d[X]";
	
	private JLabel coordinates;
	private JLabel primitive;
	private JCheckBox switchTSVisibility;
	
	private DecimalFormat values = new DecimalFormat("###.###");

	/**
	 * Creates status bar. Following parameters are used to duplicate check box button "show time series" from menu 
	 * @param actions Action manager used to get the name of button.
	 * @param showTimeSeriesModel Button model of check box.
	 */
	public StatusBar(ActionManager actions, ButtonModel showTimeSeriesModel) {
		addComponentListener(this);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		ResourceBundle labelsRB = ResourceBundle.getBundle("ui.labels");
			
		coordinates = new JLabel();
		coordinates.setToolTipText(labelsRB.getString("stat_coord"));
		coordinates.setFont(LABEL_FONT);
		coordinates.setBorder(LABEL_BORDER);
		coordinates.setMaximumSize(new Dimension(120, MAXIMUM_HEIGHT));
		add(coordinates);
		
		primitive = new JLabel();
		primitive.setToolTipText(labelsRB.getString("stat_select"));
		primitive.setFont(LABEL_FONT);
		primitive.setPreferredSize(new Dimension(150, MAXIMUM_HEIGHT));
		primitive.setBorder(LABEL_BORDER);
		add(primitive);
		
		switchTSVisibility = new JCheckBox(actions.getAction(ActionType.SWITCH_TS_VISIBILITY).getValue(AbstractAction.NAME).toString());
		switchTSVisibility.setModel(showTimeSeriesModel);
		switchTSVisibility.setFont(LABEL_FONT);
		switchTSVisibility.setBorder(LABEL_BORDER);
		switchTSVisibility.setBorderPainted(true);
		add(switchTSVisibility);
	}

	/**
	 * Sets coordinates to be displayed.
	 * @param p Model coordinates.
	 */
	public void setCoordinates(Point2D p) {
		coordinates.setText(values.format(p.getX()) + ", " + values.format(p.getY()));
	}
	
	/**
	 * Displays no coordinates.
	 */
	public void clearCoordinates() {
		coordinates.setText("");
	}	
	
	/**
	 * Makes status bar display information about target event.
	 * Must be called each time target event is modified.
	 * 
	 * @param e target event
	 */
	public void setSelectedEvent(Event e) {
		StringBuilder text = new StringBuilder();
		text.append(getReference(e));
		if (!e.isEmpty()) {
			text.append(":");
			if (e.getTime().isSet()) {
				text.append(" ");
				text.append(property(e.getTime(), time));
			}
			if (e.getConcentration().isSet()) {
				text.append(" ");
				text.append(property(e.getConcentration(), conc));
			}
			if (e.getDerivative().isSet()) {
				text.append(" ");
				text.append(derivative(e.getDerivative()));
			}
		}
		primitive.setText(text.toString());
	}
	
	/**
	 * Makes status bar display information about target transition.
	 * Must be called each time target transition is modified.
	 * 
	 * @param t target transition
	 */
	public void setSelectedTransition(Transition t) {
		StringBuilder text = new StringBuilder();
		if (t.getLeft() != null) {
			text.append(getReference(t.getLeft()));
		}
		text.append("->");
		if (t.getRight() != null) {
			text.append(getReference(t.getRight()));
		}
		if (!t.isEmpty()) {
			text.append(":");
			if (t.getConcentration().isSet()) {
				text.append(" ");
				text.append(property(t.getConcentration(), conc));
			}
			if (t.getDerivative().isSet()) {
				text.append(" ");
				text.append(derivative(t.getDerivative()));
			}
		}
		primitive.setText(text.toString());
	}
	
	/**
	 * Makes status bar display no information about selected graphic primitive. 
	 */
	public void clearSelected() {
		primitive.setText("");
	}
	
	private String getReference(Event e) {
		return "(" + values.format(e.getTime().getCenter()) + ", " + values.format(e.getConcentration().getCenter()) + ")";  
	}
	
	private String property(Property p, String name) {
		if (p.isSet()) {
			if (p.isPoint()) {
				return name + EQUALS + values.format(p.getCenter());
			} else {
				StringBuilder res = new StringBuilder();
				if (p.hasBound(Bound.LOWER)) {
					res.append(values.format(p.getBound(Bound.LOWER)));
					res.append(LESSER);
				}
				res.append(name);
				if (p.hasBound(Bound.UPPER)) {
					res.append(LESSER);
					res.append(values.format(p.getBound(Bound.UPPER)));
				}
				return res.toString();
			}
		}
		return null;
	}

	private String derivative(Property p) {
		if (p.isSet()) {
			if (p.isPoint()) {
				return der + EQUALS + values.format(p.getBound(Bound.UPPER)); 
			} else {
				StringBuilder res = new StringBuilder();
				if (!Double.isInfinite(p.getBound(Bound.LOWER))) {
					res.append(values.format(p.getBound(Bound.LOWER)));
					res.append(LESSER);
				}
				res.append(der);
				if (!Double.isInfinite(p.getBound(Bound.UPPER))) {
					res.append(LESSER);
					res.append(values.format(p.getBound(Bound.UPPER)));
				}
				return res.toString();
			}
		}
		return null;
	}
	
	@Override
	public void componentResized(ComponentEvent e) {
		Dimension dim = new Dimension();
		dim.setSize(getSize().getWidth()-primitive.getX()-switchTSVisibility.getWidth(), MAXIMUM_HEIGHT);
		primitive.setMaximumSize(dim);
	}
	@Override
	public void componentHidden(ComponentEvent e) {}
	@Override
	public void componentMoved(ComponentEvent e) {}
	@Override
	public void componentShown(ComponentEvent e) {}
	
	
}
