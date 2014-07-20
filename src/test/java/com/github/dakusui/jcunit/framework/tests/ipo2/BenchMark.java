package com.github.dakusui.jcunit.framework.tests.ipo2;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.TestCaseGeneration;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.framework.utils.tuples.NoConstraintViolationExpectation;
import com.github.dakusui.jcunit.framework.utils.tuples.SanityExpectation;
import com.github.dakusui.jcunit.framework.utils.tuples.ValidTuplesCoveredExpectation;
import com.github.dakusui.jcunit.framework.utils.tuples.VerificationResult;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class BenchMark extends IPO2Test {
  static class TestGenerationResult {
    List<Tuple> testCases;
    List<Tuple> remainders;
  }

  protected int strength;

  @Before
  public void before() {
    this.strength = 2;
  }

  protected VerificationResult verifyTuplesSanity(Factors factors, TestGenerationResult actual) {
    return new SanityExpectation(factors).verify(actual.testCases);
  }

  protected VerificationResult verifyConstraintViolation(ConstraintManager cm, TestGenerationResult actual) {
    return new NoConstraintViolationExpectation(cm).verify(actual.testCases);
  }

  protected VerificationResult verifyCoverage(Factors factors, int strength, ConstraintManager cm, TestGenerationResult actual) {
    return new ValidTuplesCoveredExpectation(factors, strength, cm).verify(actual.testCases);
  }

  @Test
  public void benchmark3$4() {
    Factors factors = buildFactors(factorsDef(3, 4));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void benchmark3$13() {
    Factors factors = buildFactors(factorsDef(3, 13));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void benchmark4$15_3$17_2$20() {
    Factors factors = buildFactors(factorsDef(4, 15), factorsDef(3, 17), factorsDef(2, 20));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void benchmark4$1_3$30_2$35() {
    Factors factors = buildFactors(factorsDef(4, 1), factorsDef(3, 30), factorsDef(2, 35));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void benchmark2$100() {
    Factors factors = buildFactors(factorsDef(2, 100));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void benchmark10$20() {
    Factors factors = buildFactors(factorsDef(10, 20));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

}
