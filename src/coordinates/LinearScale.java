//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package coordinates;

/**
 * Basic scale -- <code>scaled=base*factor</code>.
 * 
 * @author Tomáš Vejpustek
 *
 */
public class LinearScale implements Scale {
	private double scale;
	
	/**
	 * Creates identity scale.
	 */
	public LinearScale() {
		scale = 1;
	}
	
	/**
	 * Creates scale with given <code>factor</code>.
	 */
	public LinearScale(double factor) {
		scale = factor;
	}
	
	/**
	 * Creates scale from a pair of base and scaled value.
	 * @param base
	 * @param scaled
	 */
	public LinearScale(double base, double scaled) {
		scale = scaled/base;
	}
	
	@Override
	public double getBase(double scaled) {
		return scaled/scale;
	}

	@Override
	public double getScaled(double base) {
		return base*scale;
	}
	
	@Override
	public void resize(double old1, double new1, double old2, double new2) {
		double dx = getScaled(old2)-getScaled(old1);
		double dy = new2 - new1;
		scale = dx/dy;
	}
}
