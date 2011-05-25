//Copyright (C) 2011 Tomáš Vejpustek
//Full copyright notice found in src/LICENSE.  
package ltl;

import ltl.Property.Bound;

/**
 * 
 * Stores settings used in LTL formulae (e.g. operators) and provides with basic formula-building primitives.
 * 
 * @author Tomáš Vejpustek
 */
public class FormulaBuilder {
	private static String EQUALS = "=";
	private static String LESSER = "<";
	private static String GREATER = ">";
	
	private String leftParenthesis = "(";
	private String rightParenthesis = ")";
	private String andOperator = "&";
	private String orOperator = "|";
	private String notOperator = "!";
	private String nextOperator = "X";
	private String untilOperator = "U";
	private String futureOperator = "F";
	private String globallyOperator = "G";
	
	private String concentration = "[X]";
	private String derivative = "d[X]";
	private String time = "t";
	
	/**
	 * @return LTL representation of event concentration.
	 */
	public String concentration(Event e) {
		return property(e.getConcentration(), concentration);
	}
	
	/**
	 * @return LTL representation of transition concentration.
	 */
	public String concentration(Transition t) {
		return property(t.getConcentration(), concentration);
	}
	
	/**
	 * @return LTL representation of event derivative.
	 */
	public String derivative(Event e) {
		return derivative(e.getDerivative(), derivative);
	}
	
	/**
	 * @return LTL representation of transition derivative.
	 */
	public String derivative(Transition t) {
		return derivative(t.getDerivative(), derivative);
	}
	
	/**
	 * @return LTL representation of event time.
	 */
	public String time(Event e) {
		return property(e.getTime(), time);
	}
	
	/**
	 * @return Formula constituting logical conjunction of arguments.
	 */
	public String and(String form1, String form2) {
		return parenthesise(form1) + andOperator + parenthesise(form2);
	}
	
	/**
	 * @return Formula constituting logical disjunction of arguments.
	 */
	public String or(String form1, String form2) {
		return parenthesise(form1) + orOperator + parenthesise(form2);
	}
	
	/**
	 * @return Formula constituting logical negation of <code>form</code>.
	 */
	public String not(String form) {
		return notOperator + parenthesise(form);
	}
	
	/**
	 * @return Formula in the form "X <code>form</code>".
	 */
	public String next(String form) {
		return nextOperator + parenthesise(form);
	}
	
	/**
	 * @return Formula in the form "<code>condition</code> U <code>release</code>".
	 */
	public String until(String condition, String release) {
		return parenthesise(condition) + untilOperator + parenthesise(release);
	}
	
	/**
	 * @return Formula in the form "F <code>form</code>".
	 */
	public String future(String form) {
		return futureOperator + parenthesise(form);
	}
	
	/**
	 * @return Formula in the form "G <code>form</code>".
	 */
	public String globally(String form) {
		return globallyOperator + parenthesise(form);
	}
	
	/**
	 * @return <code>form</code> surrounded by parentheses.
	 */
	private String parenthesise(String form) {
		return leftParenthesis + form + rightParenthesis;
	}
	
	/**
	 * @param variable property name
	 * @return Formula specifying property.
	 */
	private String property(Property prop, String variable) {
		if (!prop.isSet()) {
			return "";
		} else if (prop.isPoint()) {
			return variable+EQUALS+prop.getCenter();
		} else {
			String lower, upper;
			if (prop.hasBound(Bound.LOWER)) {
				lower = variable+GREATER+Double.toString(prop.getBound(Bound.LOWER));
			} else {
				lower = null;
			}
			if (prop.hasBound(Bound.UPPER)) {
				upper = variable+LESSER+Double.toString(prop.getBound(Bound.UPPER));
			} else {
				upper = null;
			}
			
			if (prop.hasBounds()) {
				return and(lower, upper);
			} else if (lower != null) {
				return lower;
			} else {
				return upper;
			}
		}
	}
	
	/**
	 * @param variable derivative name
	 * @return Formula specifying derivative.
	 */
	private String derivative(Property der, String variable) {
		if (!der.isSet()) {
			return "";
		} else if (der.isPoint()) {
			return variable+EQUALS+der.getBound(Bound.UPPER);
		} else {
			String upper, lower;
			if (Double.isInfinite(der.getBound(Bound.LOWER))) {
				lower = null;
			} else {
				lower = variable + GREATER + Double.toString(der.getBound(Bound.LOWER));
			}
			if (Double.isInfinite(der.getBound(Bound.UPPER))) {
				upper = null;
			} else {
				upper = variable + LESSER + Double.toString(der.getBound(Bound.UPPER));
			}
			if ((lower != null) && (upper != null)){
				if (der.getBound(Bound.UPPER) < der.getBound(Bound.LOWER)) {
					return or(lower, upper);
				} else {
					return and(lower, upper);
				}
			} else if (lower != null) {
				return lower;
			} else if (upper != null) {
				return upper;
			} else {
				return "";
			}
		}
	}
	
}
