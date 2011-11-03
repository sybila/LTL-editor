//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.
package xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

import ltl.Model;
import ltl.ModelChange;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import exceptions.XMLException;

import series.TimeSeriesLoader;

/**
 * Abstraction of formula as a target of editing. Comprises:
 * <ul>
 *  <li>actual formula model</li>
 *  <li>information necessary to save the formula (i.e. file)</li>
 *  <li>log of changes to formula (undo and redo functionality)</li>
 *  <li>underlying time series information (such as source, loader type, ...)</li>
 * </ul>
 * 
 * Includes functionality to access its components and store and load the formula.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class Formula {
	private static final String NAMESPACE = "http://www.fi.muni.cz/~xvejpust/TimeSeriesLTLAnnotator";
	private Model model = new Model();
	private File formulaFile = null;
	private TimeSeriesSource tsSource = null;
	private UndoStack undo = new UndoStack();
	
	/**
	 * Creates a new formula with empty model.
	 */
	public Formula() {}
	
	/**
	 * Creates a formula with specified input file. 
	 * @param formulaFile
	 */
	public Formula(File formulaFile) {
		setFormulaFile(formulaFile);
	}
	
	/**
	 * Transforms formula into XML document and saves it to the file. 
	 * @throws IllegalStateException when no output file is specified.
	 * @throws FileNotFoundException when specified file was invalid.
	 * @throws XMLException when error during XML processing occurred.
	 */
	public void save() throws FileNotFoundException, XMLException {
		if (getFormulaFile() == null) {
			throw new IllegalStateException("No output file specified.");
		}
		OutputStream os = new FileOutputStream(getFormulaFile());
		DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuild;
		try {
			docBuild = docFac.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			throw new XMLException("general", "Parser could not be configured.");
		}
		
		Document doc = docBuild.newDocument();
		Element form = (Element)model.toXML(doc);
		form.setAttribute("xmlns", NAMESPACE); //set name space
		if (tsSource != null) {
			form.appendChild(tsSource.toXML(doc));
		}
		doc.appendChild(form);
		
		TransformerFactory transFac = TransformerFactory.newInstance();
		Transformer trans;
		try {
			trans = transFac.newTransformer();
		} catch (TransformerConfigurationException tce) {
			throw new XMLException("general", "Transformer could not be configured.");
		}
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		try {
			trans.transform(new DOMSource(doc), new StreamResult(os));
		} catch (TransformerException te) {
			throw new XMLException("output", "XML Transformer could not write to the file.", te);
		}
		try {
			os.close();
		} catch (IOException ioe) {
			throw new XMLException("output", "File could not be closed.", ioe);
		}
		undo.mark();
	}
	
	/**
	 * Reads and validates the file and transforms it into formula.
	 * @throws when no input file is specified
	 * @throws FileNotFoundException when input file is invalid.
	 */
	public void load() throws FileNotFoundException, XMLException {
		if (getFormulaFile() == null) {
			throw new IllegalStateException("No input file specified.");
		}
		InputStream is = new FileInputStream(getFormulaFile());
		SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema;
		try {
			schema = sf.newSchema(getClass().getResource("formula.xsd"));
		} catch (SAXException saxe) { //schema cannot be loaded
			saxe.printStackTrace();
			throw new XMLException("schema", "Document schema could not be read.");
		}
		Validator valid = schema.newValidator();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			throw new XMLException("general", "Parser could not be configured.");
		}
		Document doc;
		try {
			doc = builder.parse(is);
		} catch (SAXException saxe) { //not an XML
			throw new XMLException("parse", "Document parse error occurred.", saxe);
		} catch (IOException ioe) {
			throw new XMLException("input", "An IO error has occurred during document parsing.", ioe);
		}
		DOMResult result = new DOMResult();
		try {
			valid.validate(new DOMSource(doc), result);
		} catch (SAXException saxe) {
			throw new XMLException("validation", "Document validation error occurred.", saxe);
		} catch (IOException ioe) {
			throw new XMLException("input", "An IO error has occurred during document validation.", ioe);
		}
		Document input = (Document)result.getNode();
		input.normalize();
		
		parseXML(input.getDocumentElement());
		
		try {
			is.close();
		} catch (IOException ioe) {
			throw new XMLException("input", "Could not close the input file.", ioe);
		}
	}
		
	/**
	 * Parses XML document into formula.
	 * @param root root Element of the document.
	 * @throws XMLException
	 */
	private void parseXML(Element root) throws XMLException {
		TimeSeriesSource newTsSource = new TimeSeriesSource();
		boolean hasTsSource = false;
		newTsSource.setFormulaFile(formulaFile);
		
		NodeList nodes = root.getChildNodes();
		for (int index = 0; index < nodes.getLength(); index++) {
			Node n = nodes.item(index);
			if (n.getNodeName().equals("series")) { //template
				newTsSource.loadFromXML(n);
				hasTsSource = true;
			}
		}
		Model newModel = new Model();
		newModel.loadFromXML(root);
		
		//so far without errors -- time to replace
		model = newModel;
		if (hasTsSource) {
			tsSource = newTsSource;
		}
	}
	
	/**
	 * @return Model of the formula.
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * Apply a change to model and put it to undo stack.
	 * @param target Change to be made.
	 */
	public void applyChange(ModelChange target) {
		target.apply(model);
		undo.apply(target);
	}
	
	/**
	 * Undo the last change made to model and move it in undo stack.
	 */
	public void undo() {
		ModelChange target = undo.undo();
		if (target != null) {
			target.undo(model);
		}
	}
	
	/**
	 * Redo the last change undone to model and move it in undo stack.
	 */
	public void redo() {
		ModelChange target = undo.redo();
		if (target != null) {
			target.redo(model);
		}
	}
	
	/**
	 * @return <code>false</code> if the formula has changed since the last save, <code>true</code> otherwise.
	 */
	public boolean isSaved() {
		return !undo.hasChanged();
	}
	
	/**
	 * Removes all graphic primitives from the model.
	 */
	public void clearModel() {
		model.clear();
	}
	
	/**
	 * @return Source of the time series.
	 */
	public TimeSeriesSource getTimeSeriesSource() {
		return tsSource;
	}

	/**
	 * Adds new source of time series to this formula.
	 * @param sourceFile Souce file of time series
	 * @param loader loader of time series
	 */
	public void setTimeSeriesSource(File sourceFile, TimeSeriesLoader loader) {
		tsSource = new TimeSeriesSource();
		tsSource.setSourceFile(sourceFile);
		tsSource.setLoader(loader);
	}
	
	/**
	 * Removes source of time series from this formula.
	 */
	public void removeTimeSeriesSource() {
		tsSource = null;
	}
	
	/**
	 * Specifies file containing the formula.
	 */
	public void setFormulaFile(File formulaFile) {
		this.formulaFile = formulaFile;
		if (tsSource != null) {
			tsSource.setFormulaFile(formulaFile);
		}
	}
	
	/**
	 * @return File containing the formula.
	 */
	public File getFormulaFile() {
		return formulaFile;
	}
	
}