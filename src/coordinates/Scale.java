//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package coordinates;

/**
 * Scaling of on-screen coordinate into model coordinate and vice versa.
 * 
 * @author Tomáš Vejpustek
 *
 */
public interface Scale {
	
	/**
	 * @param scaled model coordinate.
	 * @return On-screen coordinate.
	 */
	public double getBase(double scaled);
	
	/**
	 * @param base on-screen coordinate.
	 * @return Model coordinate.
	 */
	public double getScaled(double base);
	
	/**
	 * Resizes scale so that model coordinate <code>getScaled(old1)</code> is now transformed
	 * to on-screen coordinate <code>new1</code> and similarily for <code>old2</code> and <code>new2</code>.
	 */
	public void resize(double old1, double new1, double old2, double new2);
}
