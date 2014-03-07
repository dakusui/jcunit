package com.github.dakusui.petronia.ut;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import com.github.dakusui.enumerator.Combinator;
import com.github.dakusui.enumerator.Enumerator;
import com.github.dakusui.jcunit.generators.ipo.IPO;
import com.github.dakusui.jcunit.generators.ipo.TestRun;
import com.github.dakusui.jcunit.generators.ipo.TestRunSet;
import com.github.dakusui.jcunit.generators.ipo.TestSpace;
import com.github.dakusui.jcunit.generators.ipo.ValuePair;

public class IPOTest {
	@Test
	public void ipo_01() {
		TestSpace space = new TestSpace(
			new Object[][]{
				{
					1,   2
				},
				{
					"A", "B", "C"
				},
			}
		);
		
		runTest(space);
	}

	@Test
	public void ipo_02() {
		TestSpace space = new TestSpace(
			new Object[][]{
				{
					1,   2
				},
				{
					"A", "B", "C"
				},
				{
					"a", "b", "c", "d"
				},
			}
		);
		
		runTest(space);
	}

	@Test
	public void ipo_03() {
		TestSpace space = new TestSpace(
			new Object[][]{
				{
					1,   2
				},
				{
					"A", "B", "C"
				},
				{
					"a", "b", "c", "d", "e", "f", "g", "h"
				},
			}
		);
		
		runTest(space);
	}

	@Test
	public void ipo_04() {
		TestSpace space = new TestSpace(
			new Object[][]{
				{
					1,   2
				},
				{
					"A", "B", "C"
				},
				{
					"a", "b", "c"
				},
				{
					"X", "Y", "Z", "W"
				}
			}
		);
		
		runTest(space);
	}

	@Test
	public void ipo_05() {
		TestSpace space = new TestSpace(
			new Object[][]{
				{
					1,   2,   3,   4,   5
				},
				{
					"A", "B", "C", "D"
				},
				{
					"a", "b", "C"
				},
				{
					"X", "Y"
				},
				{
					"Z"
				}
			}
		);
		
		runTest(space);
	}
	@Test
	public void ipo_06() {
		TestSpace space = new TestSpace(
			new Object[][]{
				{
					"Z"
				},
				{
					"X", "Y"
				},
				{
					"a", "b", "C"
				},
				{
					"A", "B", "C", "D"
				},
				{
					1,   2,   3,   4,   5
				},
			}
		);
		
		runTest(space);
	}

	@Test
	public void ipo_07() {
		TestSpace space = new TestSpace(
			new Object[][]{
				{
					1,
				},
				{
					"A",
				},
				{
					"a", "b", "c", "d", "e", "f"
				}
			}
		);
		
		runTest(space);
	}

	@Test
	public void ipo_08() {
		TestSpace space = new TestSpace(
			new Object[][]{
				{
					"A1", "A2", "A3"
				},
				{
					"B1", "B2", "B3", "B4"
				},
				{
					"C1", "C2", "C3", "C4", "C5"
				}
			}
		);
		
		runTest(space);
	}

	private void runTest(TestSpace space) {
		IPO ipo = new IPO(space);
		TestRunSet testRunSet = ipo.ipo();
		int i = 1;
		for (TestRun r : testRunSet) {
			System.out.printf("%03d:%s\n", i++, r);
		}
		TestCase.assertTrue(examineTestRunSetContains(testRunSet, allPossibleValuePairs(space.domains())));
		TestCase.assertTrue(examineAllTestRunsDontViolateParameterDomains(testRunSet, space.domains()));
	}
	
	private boolean examineTestRunSetContains(TestRunSet testRunSet, List<ValuePair> valuePairs) {
		boolean ret = true;
		for (ValuePair pair : valuePairs) {
			List<TestRun> matchingRuns = IPO.lookUp(IPO.lookUp(testRunSet, pair.A(), pair.r()), pair.B(), pair.s());
			System.out.printf("%s: %d hits %s\n", pair, matchingRuns.size(), matchingRuns.size() == 0 ? "ERR!" : "");
			ret &= matchingRuns.size() > 0;
		}
		return ret;
	}
	
	private boolean examineAllTestRunsDontViolateParameterDomains(TestRunSet testRunSet, Object[][] domains) {
		boolean ret = true;
		for (TestRun run : testRunSet) {
			for (int i = 1; i <= run.width(); i++) {
				boolean validValue = false;
				ret &= (validValue = ArrayUtils.contains(domains[i - 1], run.get(i)));
				if (!validValue) {
					System.out.println("F" + i + "=" + run.get(i) + "is invalid");
				}
			}
		}
		return ret;
	}
	
	
	
	List<ValuePair> allPossibleValuePairs(Object[][] domains) {
		List<ValuePair> ret = new ArrayList<ValuePair>();
		List<Object[]> v = new ArrayList<Object[]>();
		for (Object[] d : domains) {
			v.add(d);
		}
		Enumerator<Object[]> combinator = new Combinator<Object[]>(v, 2);
		while (combinator.hasNext()) {
			List<Object[]> cur = combinator.next();
			int A = indexOf(cur.get(0), domains) + 1;
			assert A != 0;
			for (Object r : cur.get(0)) {
				int B = indexOf(cur.get(1), domains) + 1;
				assert B != 0;
				for (Object s : cur.get(1)) {
					assert A != B;
					ret.add(new ValuePair(A, r, B, s));
				}
			}
		}
		return ret;
	}
	
	int indexOf(Object[] cur, Object[][] domains) {
		int i = -1;
		for (i = 0; i < domains.length; i++) {
			if (domains[i] == cur) return i;
		}
		return i;
	}
}
