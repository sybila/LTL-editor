//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ui;

import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.ButtonModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import ui.ActionManager.ActionType;

/**
 * Builds menus used by application and assigns them actions managed by {@link ActionManager}.
 * 
 * For a given menu type generally includes two methods -- <code>get&lt;menu type&gt;Menu</code>
 * and <code>get&lt;menu type&gt;PopupMenu</code> (which so far only uses the previous method and extracts
 * its popup menu). 
 * 
 * @author Tomáš Vejpustek
 *
 */
public class MenuFactory {
	private ActionManager actions;
	private ResourceBundle labels;
	
	/**
	 * Loads resource bundle containing menu labels.
	 * @param actions
	 */
	public MenuFactory(ActionManager actions) {
		this.actions = actions;
		labels = ResourceBundle.getBundle("ui.labels");
	}
	
	/**
	 * @return "File" menu 
	 */
	public JMenu getFileMenu() {
		JMenuItem item;
		JMenu menu = new JMenu(labels.getString("menu_file"));
		menu.setMnemonic(KeyEvent.VK_F);
		
		//Time Series
		item = new JMenuItem(actions.getAction(ActionType.TS_CLEAR));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
		menu.add(item);
		
		menu.add(getLoadTimeSeriesMenu());
		
		menu.addSeparator();
		
		item = new JMenuItem(actions.getAction(ActionType.FORM_NEW));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		
		item = new JMenuItem(actions.getAction(ActionType.FORM_LOAD));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		
		item = new JMenuItem(actions.getAction(ActionType.FORM_SAVE));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		
		item = new JMenuItem(actions.getAction(ActionType.FORM_SAVE_AS));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
		menu.add(item);
		
		menu.addSeparator();
		
		menu.add(actions.getAction(ActionType.EXPORT_BITMAP));
		menu.add(actions.getAction(ActionType.SHOW_FORMULA));
		
		item = new JMenuItem(actions.getAction(ActionType.EXPORT_FORMULA));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		
		menu.addSeparator();
		
		//Exit
		item = new JMenuItem(actions.getAction(ActionType.EXIT));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		
		return menu;
	}
	
	/**
	 * @return "Mode" menu which allows to choose working mode.
	 */
	public JMenu getModeMenu() {
		JMenu menu = new JMenu(labels.getString("menu_mode"));
		menu.setMnemonic(KeyEvent.VK_M);
		JMenuItem item = new JMenuItem(actions.getAction(ActionType.CREATE_EVENTS));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		item = new JMenuItem(actions.getAction(ActionType.EDIT_MODEL));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		item = new JMenuItem(actions.getAction(ActionType.MOVE_EVENTS));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		item = new JMenuItem(actions.getAction(ActionType.DELETE));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_DOWN_MASK));
		menu.add(item);
		return menu;
	}
	
	/**
	 * @param switchTimeSeriesVisibilityModel visibility model shared across several check boxes denoting visibility of time seires. 
	 * @return "View" menu used to change visual interface properties.
	 */
	public JMenu getViewMenu(ButtonModel switchTimeSeriesVisibilityModel) {
		JMenu menu = new JMenu(labels.getString("menu_view"));
		menu.setMnemonic(KeyEvent.VK_V);
		JMenuItem item = new JCheckBoxMenuItem(actions.getAction(ActionType.SWITCH_TS_VISIBILITY));
		item.setModel(switchTimeSeriesVisibilityModel);
		menu.add(item);
		return menu;
	}
	
	/**
	 * @return "Load Time Series" menu
	 */
	public JMenu getLoadTimeSeriesMenu() {
		JMenu menu = new JMenu(labels.getString("menu_load_time_series"));
		menu.add(actions.getAction(ActionType.TS_LOAD_CSV));
		return menu;
	}

	/**
	 * @return "Edit" menu
	 */
	public JMenu getEditMenu() {
		JMenu menu = new JMenu(labels.getString("menu_edit"));
		menu.setMnemonic(KeyEvent.VK_E);
		JMenuItem item = new JMenuItem(actions.getAction(ActionType.DELETE_PRIMITIVE));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		menu.add(item);
		return menu;
	}
	
}
