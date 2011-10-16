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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
	private Model model;
	private File formulaFile = null;
	
	/**
	 * Creates a new formula with empty model.
	 */
	public Formula() {
		model = new Model();
	}
	
	/**
	 * Creates a formula with specified input file. 
	 * @param formulaFile
	 */
	public Formula(File formulaFile) {
		this.formulaFile = formulaFile;
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
		NodeList nodes = root.getChildNodes();
		for (int index = 0; index < nodes.getLength(); index++) {
			Node n = nodes.item(index);
			if (n.getNodeName().equals("")) { //template
				//TODO a stub
			}
		}
		Model newModel = new Model();
		newModel.loadFromXML(root);
		
		//so far without errors -- time to replace
		model = newModel;
	}
	
	/**
	 * @return Model of the formula.
	 */
	public Model getModel() {
		return model;
	}
	
	/**
	 * Specifies file containing the formula.
	 */
	public void setFormulaFile(File formulaFile) {
		this.formulaFile = formulaFile;
	}
	
	/**
	 * @return File containing the formula.
	 */
	public File getFormulaFile() {
		return formulaFile;
	}
	
}