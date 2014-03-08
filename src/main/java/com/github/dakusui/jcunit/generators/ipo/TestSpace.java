package com.github.dakusui.jcunit.generators.ipo;

/**
 * A class that describes the test space in which the test runs generated
 * by <code>IPO</code> class should cover. 
 * 
 * @author hiroshi
 */
public class TestSpace {
	/**
	 * The value domains. Each element of this field represents the domain of
	 * each parameter.
	 */
	Object[][] domains;
	
	/**
	 * Creates an object of this class.
	 * 
	 * @param domains Domains of the parameters.
	 */
	public TestSpace(Object[][] domains) {
		if (domains == null) throw new NullPointerException();
		if (domains.length < 2) throw new IllegalArgumentException();
		for (Object[] d : domains) {
			if (d == null) throw new NullPointerException();
		}
		this.domains = domains;
	}
	
	/**
	 * Retruns a number of domains handled by this object.
	 * 
	 * @return A number of domains.
	 */
	int numDomains() {
		return domains.length;
	}
	/**
	 * Returns a domain for specified parameter by <code>i</code>
	 * @param i ID of the parameters. The origin is 1, not 0.
	 * @return The domain for Parameter Fi.
	 */
	Object[] domainOf(int i) {
		if (i ==0) throw new IllegalArgumentException();
		return domains[i - 1];
	}
	/**
	 * Returns nth value of parameter Fi.
	 * 
	 * @param i ID of the parameter Fi.
	 * @param n index of the parameter Fi. 
	 * @return the nth value of parameter Fi.
	 */
	Object value(int i, int n) {
		if (i ==0) throw new IllegalArgumentException();
		if (n ==0) throw new IllegalArgumentException();
		return domains[i - 1][n - 1];
	}

	public Object[][] domains() {
		return this.domains;
	}
}