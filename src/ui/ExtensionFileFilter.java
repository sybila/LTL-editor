//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ui;

import java.io.File;
import java.util.ResourceBundle;

import javax.swing.filechooser.FileFilter;

/**
 * Filters files according to their extension (i.e. the last characters of its name after ".").
 * 
 * Descriptions are read from resource bundle <code>ui.labels</code> and are in form "<code>ext_</code> + extension".
 * 
 * @author Tomáš Vejpustek
 *
 */
public class ExtensionFileFilter extends FileFilter {
	private String extension;
	
	/**
	 * Creates filter with given extension. 
	 * @param extension Filename extension (without dot).
	 */
	public ExtensionFileFilter(String extension) {
		this.extension = extension;
	}

	@Override
	public boolean accept(File arg0) {
		if (arg0.isDirectory()) {
			return true;
		}
		if (arg0.isFile()) {
			return arg0.getName().endsWith("." + extension);
		}
		return false;
	}

	@Override
	public String getDescription() {
		ResourceBundle labels = ResourceBundle.getBundle("ui.labels");
		return labels.getString("ext_" + extension);
	}

}
