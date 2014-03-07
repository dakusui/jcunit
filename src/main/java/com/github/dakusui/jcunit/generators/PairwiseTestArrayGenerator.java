package com.github.dakusui.jcunit.generators;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.github.dakusui.jcunit.generators.ipo.IPO;
import com.github.dakusui.jcunit.generators.ipo.TestRun;
import com.github.dakusui.jcunit.generators.ipo.TestRunSet;
import com.github.dakusui.jcunit.generators.ipo.TestSpace;

public class PairwiseTestArrayGenerator<T, U> extends BaseTestArrayGenerator<T, U> {
	private TestRunSet testRunSet;
	private Map<Integer, T> indexToKeyMap = new HashMap<Integer, T>();
	
	@Override
	public void init(Map<T, U[]> domains) {
		super.init(domains);
		Object[][] testSpaceDomains = new Object[this.domains.size()][];
		int i = 0;
		for (T cur: this.domains.keySet()) {
			testSpaceDomains[i++] = this.domains.get(cur);
			indexToKeyMap.put(i, cur); // since i is already incremented, put it as is. 
		}
		System.out.println(ArrayUtils.toString(testSpaceDomains));
		TestSpace space = new TestSpace(testSpaceDomains);
		IPO ipo = new IPO(space);
		this.testRunSet = ipo.ipo();
		this.size = this.testRunSet.size();
		this.cur = 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int getIndex(T key, long cur) {
		TestRun run = this.testRunSet.get((int) cur);
		// IPO classes provide 1-origin methods!
		for (int i = 1; i <= run.width(); i++) {
			T k = indexToKeyMap.get(i);
			if (key.equals(k)) {
				U[] domainOf_i = findDomain(k);
				return indexOf((U) run.get(i), domainOf_i);
			}
		}
		assert false;
		return -1;
	}

	private U[] findDomain(T key) {
		return this.domains.get(key);
	}
	
	/*
	 * returns an index of specified value by using '==' operator not by using
	 * 'equals' method.
	 */
	private int indexOf(U u, U[] domain) {
		int i = 0;
		for (Object obj : domain) {
			if (obj == u) return i;
			i++;
		}
		return -1;
	}
}
