package com.github.dakusui.jcunit.generators.ipo;

import java.util.ArrayList;

/**
 * A class that represents the set of test runs.
 * 
 * @author hiroshi
 */
public class TestRunSet extends ArrayList<TestRun> {
	private static final long serialVersionUID = 1L;
	int width;
	
	TestRunSet(int width) {
		this.width = width;
	}
	
	/**
	 * Returns number of parameters that are held by this object.
	 * 
	 * @return number of attributes.
	 */
	public int width() {
		return this.width;
	}
	
	/**
	 * Add the given <code>run</code> object to this object.
	 * If a value not to be added is given, a <code>RuntimeException</code>
	 * will be thrown.
	 * 
	 * @param run the value to be added.
	 */
	@Override
	public boolean add(TestRun run) {
		if (run == null) throw new NullPointerException();
		if (run.v == null) throw new NullPointerException();
		if (run.v.length != this.width) throw new IllegalArgumentException();
		return super.add(run);
	}
	
	/**
	 * Returns an array of parameter IDs by which you can get call as 
	 * <code>F</code> in <code>valueOf(int F, int i)</code>.
	 *  
	 * @return An array of parameter ID's.
	 */
	public int[] coveredParameters() {
		int[] ret = new int[this.width];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = i + 1;
		}
		return ret;
	}
	
	/**
	 * Returns <code>i</code>th run in this set. Note that <code>i</code> is 1-origin. 
	 * not 0.
	 * 
	 * @param i an index to specify test run.
	 * @return A <code>i</code>th test run
	 */
	public TestRun getRun(int i) {
		if (i == 0) throw new IllegalArgumentException();
		return super.get(i - 1);
	}
}