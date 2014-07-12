package com.github.dakusui.jcunit.ipo2;

import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.constraints.ConstraintObserver;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.Tuple;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.IPO2Utils;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConstraintTest extends IPO2Test {
	public static class TestConstraintManager implements ConstraintManager {
		private final Set<Tuple> constraints;

		TestConstraintManager(List<Tuple> constraints) {
			this.constraints = new HashSet<Tuple>();
			this.constraints.addAll(constraints);
		}

		private static boolean matches(Tuple constraint, Tuple t) {
			for (String fName : constraint.keySet()) {
				if (!IPO2Utils.eq(constraint.get(fName), t.get(fName))) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean check(Tuple tuple) {
			for (Tuple c : constraints) {
        if (!tuple.keySet().containsAll(c.keySet())) continue;
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

	public List<Tuple> getProhibitedTuples() {
		return this.prohibitedTuples;
	}

	protected void setProhibitedTuples(Tuple... tuples) {
		this.prohibitedTuples = new LinkedList<Tuple>();
    Collections.addAll(prohibitedTuples, tuples);
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

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
		verify(ipo.getResult(), ipo.getRemainders(), strength, factors, constraintManager);
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

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(ipo.getResult(), ipo.getRemainders(), strength, factors, constraintManager);
	}

  @Override
  protected void verifyRemaindersViolateConstraints(List<Tuple> remainders,
      List<Tuple> result, ConstraintManager constraintManager) {
    // Since in this test class there is no implicit constraint, we
    // can simply verify them.
    System.err.println(result);
    for (Tuple tuple : remainders) {
      assertThat(String.format("'%s' is contained in result set.", tuple), find(tuple, result), is(false));
      assertThat(String.format("'%s' doesn't violate any constraints.", tuple), constraintManager.check(
          tuple), is(false));
    }
  }
}
