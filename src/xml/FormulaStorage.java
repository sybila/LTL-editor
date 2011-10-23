//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.
package xml;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import exceptions.XMLException;

/**
 * Steps necessary to store formula to XML and retrieve it.
 * @deprecated moved to {@link Formula}
 * 
 * @author Tomáš Vejpustek
 * 
 */
public class FormulaStorage {
	private static final String NAMESPACE = "http://www.fi.muni.cz/~xvejpust/TimeSeriesLTLAnnotator";
	
	/**
	 * Transforms formula to XML and prints it.
	 * @param os Stream where to print the formula
	 * @param formula The formula to be transformed to XML
	 * @throws XMLException when an error during transformation or printing occurs.
	 */
	public void storeFormula(OutputStream os, XMLRepresentable formula) throws XMLException {
		DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuild;
		try {
			docBuild = docFac.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			throw new XMLException("general", "Parser could not be configured.");
		}
		
		Document doc = docBuild.newDocument();
		Element form = (Element)formula.toXML(doc);
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
	}
	
	/**
	 * Reads and validates XML document from input and transforms it into object.
	 * 
	 * @param is Stream to read from.
	 * @param output Object (or a type of object) which is loaded from XML. Warning:
	 * <b>Always use a new object, as an error during input leaves it in an undefined state.</b> 
	 * @throws XMLException when error during reading, parsing, validation or transfromation occurs.
	 */
	public void loadFormula(InputStream is, XMLRepresentable output) throws XMLException {
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
		
		output.loadFromXML(input.getDocumentElement());
	}
	
}
