//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

/**
 * Specifies object which can be represented by LTL formula.
 * 
 * @author Tomáš Vejpustek
 *
 */
public interface LTLRepresentable {

	/**
	 * @param builder Settings of formula operators.
	 * @return LTL representation of this object.
	 */
	public String toLTL(FormulaBuilder builder);
}
