package com.github.dakusui.jcunit.ipo2;

import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.constraints.ConstraintObserver;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.Tuple;
import com.github.dakusui.jcunit.generators.ipo2.IPO2Utils;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ConstraintTest extends IPO2Test {
	public static class TestConstraintManager implements ConstraintManager {
		private final Set<Tuple> constraints;

		TestConstraintManager(List<Tuple> constraints) {
			this.constraints = new HashSet<Tuple>();
			this.constraints.addAll(constraints);
		}

		private static boolean matches(Tuple constraint, Tuple t) {
			for (String fName : constraint.keySet()) {
				if (!t.containsKey(fName)) {
					return false;
				}
				if (!IPO2Utils.eq(constraint.get(fName), t.get(fName))) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean check(Tuple tuple) {
			for (Tuple c : constraints) {
				if (matches(c, tuple)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public void addObserver(ConstraintObserver observer) {

		}

		@Override
		public void removeObservers(ConstraintObserver observer) {

		}
	}

	private List<Tuple> prohibitedTuples = null;

	@Override
	public List<Tuple> getProhibitedTuples() {
		return this.prohibitedTuples;
	}

	protected void setProhibitedTuples(Tuple... tuples) {
		this.prohibitedTuples = new LinkedList<Tuple>();
		for (Tuple t : tuples) {
			this.prohibitedTuples.add(t);
		}
	}

	@Override
	public ConstraintManager createConstraintManager() {
		return new TestConstraintManager(getProhibitedTuples());
	}

	@Test
	public void test_001a() {
		int strength = 2;
		this.setProhibitedTuples(
				new Tuple.Builder().put("F1", "L1x").put("F2", "L2x").build());

		Factors factors = new Factors.Builder()
				.add(factor("F1", "L11", "L1x"))
				.add(factor("F2", "L21", "L2x"))
				.add(factor("F3", "L31"))
				.add(factor("F4", "L41", "L42")).build();
		ConstraintManager constraintManager = createConstraintManager();
		IPO2Optimizer optimizer = createOptimizer();

		List<Tuple> testcases = generate(factors,
				strength, constraintManager, optimizer);

		verify(testcases, strength, factors, constraintManager);
	}

	@Test
	public void test_001b() {
		int strength = 3;
		this.setProhibitedTuples(
				new Tuple.Builder().put("F1", "L1x").put("F2", "L2x").build());

		Factors factors = new Factors.Builder()
				.add(factor("F1", "L11", "L1x"))
				.add(factor("F2", "L21", "L2x"))
				.add(factor("F3", "L31"))
				.add(factor("F4", "L41", "L42")).build();
		ConstraintManager constraintManager = createConstraintManager();
		IPO2Optimizer optimizer = createOptimizer();

		List<Tuple> testcases = generate(factors,
				strength, constraintManager, optimizer);

		verify(testcases, strength, factors, constraintManager);
	}

}
