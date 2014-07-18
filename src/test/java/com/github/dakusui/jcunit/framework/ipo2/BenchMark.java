package com.github.dakusui.jcunit.framework.ipo2;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BenchMark extends IPO2Test {
	public static class VerificationResult {
		private final List<Tuple> invalidTestCases;
		private final List<Tuple> invalidRemainders;
		private final List<Tuple> intersectionBetweenTestCasesAndRemainders;

		public VerificationResult(List<Tuple> invalidTestCases, List<Tuple> invalidRemainders, List<Tuple> intersectionBetweenTestCasesAndRemainder) {
			this.invalidTestCases = Utils.checknotnull(invalidTestCases);
			this.invalidRemainders = Utils.checknotnull(invalidRemainders);
			this.intersectionBetweenTestCasesAndRemainders = Utils.checknotnull(invalidTestCases);
		}

		public boolean isSuccessful() {
			return this.invalidTestCases.isEmpty() && this;
		}

		public List<Tuple> getInvalidTestCases() {
			return null;
		}

		public List<Tuple> getInvalidRemainders() {
			return null;
		}

		public List<Tuple> get
	}
	public static interface Expectation {
		public boolean verify(List<Tuple> testCases, List<Tuple> remainder);
	}

	public abstract static class ExpectationBase implements  Expectation {
		private List<Tuple> invalidTestCases;

		abstract protected List<Tuple> verifyTestCases(List<Tuple> testCases);

		abstract protected List<Tuple> verifyRemainders(List<Tuple> remainders);

		abstract protected List<Tuple> verifyDisjointness(List<Tuple> testCases, List<Tuple> remainders);


		public VerificationResult verify(List<Tuple> testCases, List<Tuple> remaider) {
			List<Tuple> invalidTestCases = verifyTestCases(testCases);
			List<Tuple> invalidRemainders = verifyRemainders(testCases);
			List<Tuple> intersectionBetweenTestCasesAndRemainder = verifyDisjointness(testCases, remaider);
			return new VerificationResult(invalidTestCases, invalidRemainders, intersectionBetweenTestCasesAndRemainder);
		}
	}

	public static class BasicExpectation implements Expectation {
		private final Set<Tuple> expectedTuples;
		private final Set<Tuple> knownProhibitedTuples;

		public BasicExpectation(Set<Tuple> expectedTuples, Set<Tuple> knownProhibitedTuples) {
			this.expectedTuples = Utils.checknotnull(expectedTuples);
			this.knownProhibitedTuples = Utils.checknotnull(knownProhibitedTuples);
		}

		private boolean verifyTestCases(List<Tuple> testCases) {
			return new HashSet<Tuple>(Utils.checknotnull(testCases)).containsAll(this.expectedTuples);
		}

		protected boolean verifyRemainders(List<Tuple> remainders) {
			return Utils.checknotnull(remainders).isEmpty();
		}

		protected boolean verifyDisjointness(List<Tuple> testCases, List<Tuple> remainders) {
			return !containsAny(new HashSet<Tuple>(testCases), new HashSet<Tuple>(remainders));
		}

	}

	public static class SanityExpectation extends BasicExpectation {
		public SanityExpectation(Factors factors, int strength) {
			super()
		}
	}

	public static class FactorsDef {
		int numLevels;
		int numFactors;

		public FactorsDef(int numLevels, int numFactors) {
			this.numLevels = numLevels;
			this.numFactors = numFactors;
		}
	}

	public static <T> boolean containsAny(Collection<T> set, Collection<T> another) {
		Utils.checknotnull(set);
		for (T t : Utils.checknotnull(another)) {
			if (set.contains(t)) return true;
		}
		return false;
	}
	static FactorsDef factosDef(int l, int f) {
		return new FactorsDef(l, f);
	}

	static Factors buildFactors(FactorsDef... factorsDefs) {
		Factors.Builder fb = new Factors.Builder();
		char ch = 'A';
		for (FactorsDef fd : factorsDefs) {
			for (int i = 0; i < fd.numFactors; i++) {
				Factor.Builder b = new Factor.Builder();
				b.setName(new Character(ch).toString());
				for (int j = 0; j < fd.numLevels; j++) {
					b.addLevel(new Character(ch).toString() + j);
				}
				ch++;
				fb.add(b.build());
			}
		}
		return fb.build();
	}

	protected int strength;

	@Before
	public void before() {
		this.strength = 2;
	}

	@Test
	public void benchmark3$4() {
		Factors factors = buildFactors(factosDef(3, 4));
		ConstraintManager constraintManager = createConstraintManager();
		IPO2Optimizer optimizer = createOptimizer();

		IPO2 ipo = generate(factors,
				strength, constraintManager, optimizer);
		verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
		);
	}

	@Test
	public void benchmark3$13() {
		Factors factors = buildFactors(factosDef(3, 13));
		ConstraintManager constraintManager = createConstraintManager();
		IPO2Optimizer optimizer = createOptimizer();

		IPO2 ipo = generate(factors,
				strength, constraintManager, optimizer);
		verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
		);
	}

	@Test
	public void benchmark4$15_3$17_2$20() {
		Factors factors = buildFactors(factosDef(4, 15), factosDef(3, 17), factosDef(2, 20));
		ConstraintManager constraintManager = createConstraintManager();
		IPO2Optimizer optimizer = createOptimizer();

		IPO2 ipo = generate(factors,
				strength, constraintManager, optimizer);
		verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
		);
	}

	@Test
	public void benchmark4$1_3$30_2$35() {
		Factors factors = buildFactors(factosDef(4, 1), factosDef(3, 30), factosDef(2, 35));
		ConstraintManager constraintManager = createConstraintManager();
		IPO2Optimizer optimizer = createOptimizer();

		IPO2 ipo = generate(factors,
				strength, constraintManager, optimizer);
		verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
		);
	}

	@Test
	public void benchmark2$100() {
		Factors factors = buildFactors(factosDef(2, 100));
		ConstraintManager constraintManager = createConstraintManager();
		IPO2Optimizer optimizer = createOptimizer();

		IPO2 ipo = generate(factors,
				strength, constraintManager, optimizer);
		verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
		);
	}

	@Test
	public void benchmark10$20() {
		Factors factors = buildFactors(factosDef(10, 20));
		ConstraintManager constraintManager = createConstraintManager();
		IPO2Optimizer optimizer = createOptimizer();

		IPO2 ipo = generate(factors,
				strength, constraintManager, optimizer);
		verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
		);
	}

}
