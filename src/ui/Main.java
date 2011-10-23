//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import ltl.Model;
import series.AbstractTSLoader;
import series.CsvTSLoader;
import series.TSLoaderException;
import series.TimeSeries;
import series.TimeSeriesLoader;
import ui.ActionManager.ActionType;
import xml.Formula;
import xml.XMLException;

/**
 * Main application window and controls.
 * 
 * @author Tomáš Vejpustek
 *
 */
@SuppressWarnings("serial")
public class Main extends JFrame {
	private JPanel contentPane;
	private JToolBar toolBar;
	private JToggleButton modeCreateEvent, modeEditModel, modeMoveEvents, modeDelete;
	private JFileChooser formulaeFC = new JFileChooser();
	private StatusBar statusBar;
	private ButtonModel showTimeSeries = new DefaultButtonModel();
	
	private ActionManager actions = new ActionManager();
	private WorkSpace workspace;
	
	private ResourceBundle labelsRB = ResourceBundle.getBundle("ui.labels");
	private ResourceBundle messagesRB = ResourceBundle.getBundle("ui.messages");
	
	private File timeSeriesFolder = new File(System.getProperty("user.home"));
	private File exportFolder = new File(System.getProperty("user.home"));
	
	private Formula formula = new Formula();

	/**
	 * Launches the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main frame = new Main();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Creates the frame.
	 */
	public Main() {
		setDefaultTitle();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 810, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		formulaeFC.setFileFilter(new ExtensionFileFilter("xml"));
		
		statusBar = new StatusBar(actions, showTimeSeries);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		
		workspace = new WorkSpace(this, statusBar);
		contentPane.add(workspace, BorderLayout.CENTER);
		
		toolBar = new JToolBar();
		contentPane.add(toolBar, BorderLayout.NORTH);
		toolBar.setFloatable(false);
		toolBar.add(actions.getAction(ActionType.FORM_NEW));
		toolBar.add(actions.getAction(ActionType.FORM_LOAD));
		toolBar.add(actions.getAction(ActionType.FORM_SAVE));
		toolBar.add(actions.getAction(ActionType.EXPORT_FORMULA));
		toolBar.addSeparator();
		
		ButtonGroup modes = new ButtonGroup();
		modeCreateEvent = new JToggleButton(actions.getAction(ActionType.CREATE_EVENTS));
		modeCreateEvent.setSelected(true);
		modes.add(modeCreateEvent);
		toolBar.add(modeCreateEvent);
		modeEditModel = new JToggleButton(actions.getAction(ActionType.EDIT_MODEL));
		modes.add(modeEditModel);
		toolBar.add(modeEditModel);
		modeMoveEvents = new JToggleButton(actions.getAction(ActionType.MOVE_EVENTS));
		modes.add(modeMoveEvents);
		toolBar.add(modeMoveEvents);
		modeDelete = new JToggleButton(actions.getAction(ActionType.DELETE));
		modes.add(modeDelete);
		toolBar.add(modeDelete);
		
		toolBar.addSeparator();
		toolBar.add(actions.getAction(ActionType.DELETE_PRIMITIVE));
		
		//menus
		setJMenuBar(new JMenuBar());
		MenuFactory menus = new MenuFactory(actions);
		getJMenuBar().add(menus.getFileMenu());
		getJMenuBar().add(menus.getModeMenu());
		getJMenuBar().add(menus.getEditMenu());
		getJMenuBar().add(menus.getViewMenu(showTimeSeries));
		
		//set some actions
		actions.setAction(ActionType.EXIT, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		actions.setAction(ActionType.TS_LOAD_CSV, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(timeSeriesFolder);
				fc.setFileFilter(new ExtensionFileFilter("csv"));
				int retVal = fc.showOpenDialog(Main.this);
				if (retVal == JFileChooser.APPROVE_OPTION) {
					BufferedReader input;
					TimeSeries series;
					try {
						input = new BufferedReader(new InputStreamReader(new FileInputStream(fc.getSelectedFile())));
					} catch (FileNotFoundException fnfe) {
						JOptionPane.showMessageDialog(Main.this, MessageFormat.format(messagesRB.getString("err_fnf_inf"), fc.getSelectedFile().toString()), labelsRB.getString("err_input"), JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					try {
						TimeSeriesLoader loader = new CsvTSLoader(input);
						series = new TimeSeries(loader);
						workspace.setTimeSeries(series);
						formula.setTimeSeriesSource(fc.getSelectedFile(), loader);
						actions.getAction(ActionType.SWITCH_TS_VISIBILITY).setEnabled(true);
						showTimeSeries.setSelected(true);
					} catch (TSLoaderException tsle) {
						JOptionPane.showMessageDialog(Main.this, tsle.getLocalizedMessage(), labelsRB.getString("err_input"), JOptionPane.ERROR_MESSAGE);
						return;
					} finally {
						try {
							input.close();
						} catch (IOException ioe) {
							JOptionPane.showMessageDialog(Main.this, MessageFormat.format(messagesRB.getString("err_io_close"), fc.getSelectedFile()) + "\n" + ioe.getLocalizedMessage(), labelsRB.getString("err_input"), JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
				}
				timeSeriesFolder = fc.getCurrentDirectory();
			}
		});
		actions.setAction(ActionType.TS_CLEAR, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				workspace.setTimeSeries(null);
				formula.removeTimeSeriesSource();
				actions.getAction(ActionType.SWITCH_TS_VISIBILITY).setEnabled(false);
			}
		});
		
		actions.setAction(ActionType.FORM_SAVE_AS, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int retVal = formulaeFC.showSaveDialog(Main.this);
				if (retVal == JFileChooser.APPROVE_OPTION && checkFileWrite(formulaeFC.getSelectedFile())) {
					formula.setFormulaFile(formulaeFC.getSelectedFile());
					setNameTitle(formula.getFormulaFile().toString());
					saveFormula();
				}
			}
		});
		actions.setAction(ActionType.FORM_SAVE, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (formula.getFormulaFile() == null) {
					actions.getAction(ActionType.FORM_SAVE_AS).actionPerformed(e);
				} else {
					saveFormula();
				}
			}
		});
		actions.setAction(ActionType.FORM_LOAD, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkSaved()) {
					int retVal = formulaeFC.showOpenDialog(Main.this);
					if (retVal == JFileChooser.APPROVE_OPTION) {
						try {
							workspace.unselect();
							formula.setFormulaFile(formulaeFC.getSelectedFile());
							formula.load();
							workspace.refresh();
							setNameTitle(formula.getFormulaFile().toString());
						} catch (FileNotFoundException fnfe) {
							JOptionPane.showMessageDialog(Main.this, MessageFormat.format(messagesRB.getString("err_fnf_out"), formulaeFC.getSelectedFile().toString()), labelsRB.getString("err_input"), JOptionPane.ERROR_MESSAGE);
							return;
						} catch (XMLException xmle) {
							JOptionPane.showMessageDialog(Main.this, xmle.getLocalizedMessage(), labelsRB.getString("err_input"), JOptionPane.ERROR_MESSAGE);
							return;
						}
						try {
							TimeSeriesLoader loader = AbstractTSLoader.getLoader(formula.getTimeSeriesSource());
							TimeSeries series = new TimeSeries(loader);
							workspace.setTimeSeries(series);
							actions.getAction(ActionType.SWITCH_TS_VISIBILITY).setEnabled(true);
							showTimeSeries.setSelected(true);
						} catch (FileNotFoundException fnfe) {
							JOptionPane.showMessageDialog(Main.this, MessageFormat.format(messagesRB.getString("ts_not_loaded"), fnfe.getLocalizedMessage()), labelsRB.getString("err_input"), JOptionPane.WARNING_MESSAGE);
						} catch (TSLoaderException tsle) {
							JOptionPane.showMessageDialog(Main.this, MessageFormat.format(messagesRB.getString("ts_not_loaded"), tsle.getLocalizedMessage()), labelsRB.getString("err_input"), JOptionPane.WARNING_MESSAGE);
						}
						workspace.repaint();
					}
				}
			}
		});
		actions.setAction(ActionType.FORM_NEW, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkSaved()) {
					workspace.clearModel();
					formula.setFormulaFile(null);
					formula.clearModel();
					setDefaultTitle();
					workspace.repaint();
				}	
			}
		});
		
		actions.setAction(ActionType.CREATE_EVENTS, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				workspace.modeCreateEvents();
				modeCreateEvent.setSelected(true);
			}
		});
		actions.setAction(ActionType.EDIT_MODEL, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				workspace.modeEditModel();
				modeEditModel.setSelected(true);
			}
		});
		actions.setAction(ActionType.MOVE_EVENTS, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				workspace.modeMoveEvents();
				modeMoveEvents.setSelected(true);
			}
		});
		actions.setAction(ActionType.DELETE, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				workspace.modeDelete();
				modeDelete.setSelected(true);
			}
		});	
		
		actions.setAction(ActionType.EXPORT_BITMAP, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				workspace.unselect();
				JFileChooser fc = new JFileChooser(exportFolder);
				fc.setFileFilter(new ExtensionFileFilter("png"));
				int retVal = fc.showSaveDialog(Main.this);
				if (retVal == JFileChooser.APPROVE_OPTION && checkFileWrite(fc.getSelectedFile())) {
					BufferedImage out = new BufferedImage(workspace.getWidth(), workspace.getHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics g = out.createGraphics();
					workspace.paint(g);
					g.dispose();
					try {
						ImageIO.write(out, "png", fc.getSelectedFile());
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(Main.this, MessageFormat.format(messagesRB.getString("err_io_out"), fc.getSelectedFile().getName()), labelsRB.getString("err_output"), JOptionPane.ERROR_MESSAGE);
					}
				}
				exportFolder = fc.getCurrentDirectory();
			}
		});
		actions.setAction(ActionType.SHOW_FORMULA, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String formula = workspace.getFormula();
				if (formula.isEmpty()) {
					JOptionPane.showMessageDialog(Main.this, messagesRB.getString("ltl_formula_empty"), labelsRB.getString("msg_ltl"), JOptionPane.INFORMATION_MESSAGE);
				} else {
					JTextArea formDisplay = new JTextArea(formula);
					formDisplay.setEditable(false);
					formDisplay.setLineWrap(true);
					formDisplay.setColumns(50);
					formDisplay.setSize(formDisplay.getPreferredSize().width, 1);
					JOptionPane.showMessageDialog(Main.this, formDisplay, labelsRB.getString("msg_ltl"), JOptionPane.PLAIN_MESSAGE);
				}
			}
		});
		actions.setAction(ActionType.EXPORT_FORMULA, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String formula = workspace.getFormula();
				if (formula.isEmpty()) {
					JOptionPane.showMessageDialog(Main.this, messagesRB.getString("ltl_formula_empty"), labelsRB.getString("err_output"), JOptionPane.ERROR_MESSAGE);
				} else {
					JFileChooser fc = new JFileChooser(exportFolder);
					fc.setFileFilter(new ExtensionFileFilter("ltl"));
					int retVal = fc.showSaveDialog(Main.this);
					if (retVal == JFileChooser.APPROVE_OPTION && checkFileWrite(fc.getSelectedFile())) {
						BufferedWriter out;
						try {
							out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fc.getSelectedFile())));
						} catch (FileNotFoundException fnfe) {
							JOptionPane.showMessageDialog(Main.this, MessageFormat.format(messagesRB.getString("err_fnf_out"), fc.getSelectedFile()), labelsRB.getString("err_output"), JOptionPane.ERROR_MESSAGE);
							return;
						}
						try {
							out.write(formula);
						} catch (IOException ioe) {
							JOptionPane.showMessageDialog(Main.this, MessageFormat.format(messagesRB.getString("err_io_close"), fc.getSelectedFile()), labelsRB.getString("err_output"), JOptionPane.ERROR_MESSAGE);
						} finally {
							try {
								out.close();
							} catch (IOException ioe) {
								JOptionPane.showMessageDialog(Main.this, MessageFormat.format(messagesRB.getString("err_io_close"), fc.getSelectedFile()), labelsRB.getString("err_output"), JOptionPane.ERROR_MESSAGE);
							}
						}
					}
					exportFolder = fc.getCurrentDirectory();
				}
			}
		});
	
		actions.setAction(ActionType.SWITCH_TS_VISIBILITY, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean state = workspace.switchTimeSeriesVisible();
				showTimeSeries.setSelected(state);
			}
		});
		actions.getAction(ActionType.SWITCH_TS_VISIBILITY).setEnabled(false);
		showTimeSeries.setSelected(true);
		
		actions.setAction(ActionType.DELETE_PRIMITIVE, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setPrimitiveSelected(false);
				workspace.deleteSelected();
				
			}
		});
		setPrimitiveSelected(false);
	}
	
	/**
	 * Saves current formula to file specified by the user interface.
	 */
	private void saveFormula() {
		if (formula.getFormulaFile() == null) {
			throw new NullPointerException("No file to save formula selected.");
		}
		try {
			workspace.unselect();
			formula.save();
		} catch (FileNotFoundException fnfe) {
			JOptionPane.showMessageDialog(this, MessageFormat.format(messagesRB.getString("err_fnf_out"), formula.getFormulaFile().toString()), labelsRB.getString("err_output"), JOptionPane.ERROR_MESSAGE);
			formula.setFormulaFile(null);
			setDefaultTitle();
		} catch (XMLException xmle) {
			JOptionPane.showMessageDialog(this, xmle.getLocalizedMessage(), labelsRB.getString("err_output"), JOptionPane.ERROR_MESSAGE);
			formula.setFormulaFile(null);
			setDefaultTitle();
		}
	}
	
	/**
	 * Checks if the current formula is saved. If not, asks user whether to continue and proceeds accordingly.
	 * 
	 * Used before opening a formula or creating a new one. 
	 * 
	 * @return <code>true</code> if formula is saved or the user did not want to save it, <code>false</code> if the user wanted to cancel the operation.
	 */
	public boolean checkSaved() {
		return true;
	}
	
	/**
	 * Informs the frame whether graphical primitive is selected and consequently enables or disables
	 * related actions (edit primitive and delete primitive).
	 * 
	 * @param value <code>true</code> when a primitive is selected, <code>false</code> otherwise.
	 */
	public void setPrimitiveSelected(boolean value) {
		actions.getAction(ActionType.DELETE_PRIMITIVE).setEnabled(value);
	}
	
	/**
	 * Makes the title of window default (i.e. "Time Series LTL Annotator"). 
	 */
	private void setDefaultTitle() {
		setTitle(labelsRB.getString("main_title"));
	}
	
	/**
	 * Incorporates <code>name</code> in the window title (i.e. "[untitled] - Time Series LTL Annotator").
	 */
	private void setNameTitle(String name) {
		setTitle(name + " - " + labelsRB.getString("main_title"));
	}
	
	/**
	 * Checks whether the file <code>out</code> exists and when it does, consults the user whether it should be overwritten.
	 * @return <code>true</code> when the file may be written into, <code>false</code> when the file would be overwritten and the user does not want it to be.
	 */
	private boolean checkFileWrite(File out) {
		if (!out.exists()) {
			return true;
		}
		int retVal = JOptionPane.showConfirmDialog(Main.this, MessageFormat.format(messagesRB.getString("opt_file_exists"), out), labelsRB.getString("opt_file_exists"), JOptionPane.YES_NO_OPTION);
		return (retVal == JOptionPane.YES_OPTION);
	}
	
	/**
	 * @return Model of formula being edited.
	 */
	public Model getModel() {
		return formula.getModel();
	}

}
