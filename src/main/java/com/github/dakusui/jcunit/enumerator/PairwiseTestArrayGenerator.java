package com.github.dakusui.jcunit.enumerator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.github.dakusui.jcunit.enumerator.ipo.IPO;
import com.github.dakusui.jcunit.enumerator.ipo.IPO.Run;
import com.github.dakusui.jcunit.enumerator.ipo.IPO.TestRunSet;
import com.github.dakusui.jcunit.enumerator.ipo.IPO.TestSpace;

public class PairwiseTestArrayGenerator<T, U> extends BaseTestArrayGenerator<T, U> {
	private TestRunSet testRunSet;
	private Map<Integer, T> indexToKeyMap = new HashMap<Integer, T>();
	
	@Override
	public void init(Map<T, U[]> domains) {
		super.init(domains);
		Object[][] testSpaceDomains = new Object[this.domains.size()][];
		int i = 0;
		for (T cur: this.domains.keySet()) {
			System.out.println("--- " + cur);
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
	protected Map<T, U> get(long cur) {
		Run run = this.testRunSet.get((int) cur);
		Map<T, U> ret = new LinkedHashMap<T, U>();
		// IPO classes provide 1-origin methods!
		for (int i = 1; i <= run.width(); i++) {
			ret.put(indexToKeyMap.get(i), (U) run.get(i));
		}
		return ret;
	}

}
