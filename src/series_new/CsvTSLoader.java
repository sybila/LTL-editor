//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.
package series_new;

import java.io.BufferedReader;

/**
 * Very primitive specialization of {@link FieldTSLoader}, where fields are separated by `,' and time,
 * concentration and its derivative are located in the first, second and third columns, respectively.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class CsvTSLoader extends FieldTSLoader {

	/**
	 * Creates default loader from CSV.
	 * @param input Input stream
	 * @throws TSLoaderException
	 */
	public CsvTSLoader(BufferedReader input) throws TSLoaderException {
		super(input, ",", 0, 1, 2);
	}

}
