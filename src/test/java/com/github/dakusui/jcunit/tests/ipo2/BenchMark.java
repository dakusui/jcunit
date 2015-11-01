package com.github.dakusui.jcunit.tests.ipo2;

import com.github.dakusui.jcunit.core.StringUtils;
import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManager;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.ututils.PredicateExpectation;
import com.github.dakusui.jcunit.ututils.tuples.NoConstraintViolationExpectation;
import com.github.dakusui.jcunit.ututils.tuples.SanityExpectation;
import com.github.dakusui.jcunit.ututils.tuples.ValidTuplesCoveredExpectation;
import com.github.dakusui.jcunit.ututils.tuples.VerificationResult;
import com.github.dakusui.jcunit.plugins.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.plugins.generators.ipo2.optimizers.IPO2Optimizer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.LinkedList;
import java.util.List;

public class BenchMark extends IPO2Test {
  @Rule
  public TestName name = new TestName();
  protected int strength;

  @Before
  public void before() {
    this.strength = 2;
  }

  protected void verify(Factors factors, int strength, ConstraintManager cm,
      TestGenerationResult actual) {
    System.out.println(StringUtils.format(
        "%-40s:(testcases, remainders, time(sec))=(%4s, %4s, %s)",
        name.getMethodName(),
        actual.testCases.size(),
        actual.remainders.size(),
        ((double) actual.timeSpent) / 1000
    ));
    List<VerificationResult> verificationResults = new LinkedList<VerificationResult>();
    verificationResults.add(verifyTuplesSanity(factors, actual));
    verificationResults.add(verifyCoverage(factors, strength, cm, actual));
    verificationResults.add(verifyConstraintViolation(cm, actual));
    verificationResults.add(
        verifyUnnecessaryRemainders(doesViolateConstraint(cm), actual));
    List<String> messages = new LinkedList<String>();
    for (VerificationResult vr : verificationResults) {
      if (!vr.isSuccessful()) {
        messages.add(vr.composeErrorReport());
      }
    }
    if (!messages.isEmpty()) {
      throw new AssertionError(StringUtils.join("\n", messages));
    }
  }

  protected VerificationResult verifyTuplesSanity(Factors factors,
      TestGenerationResult actual) {
    return new SanityExpectation(factors).verify(actual.testCases);
  }

  protected VerificationResult verifyConstraintViolation(ConstraintManager cm,
      TestGenerationResult actual) {
    return new NoConstraintViolationExpectation(cm).verify(actual.testCases);
  }

  protected VerificationResult verifyCoverage(Factors factors, int strength,
      ConstraintManager cm, TestGenerationResult actual) {
    return new ValidTuplesCoveredExpectation(factors, strength, cm)
        .verify(actual.testCases);
  }

  protected VerificationResult verifyUnnecessaryRemainders(
      final PredicateExpectation.Predicate isInevitableRemainder,
      TestGenerationResult actual) {
    return PredicateExpectation.any(new PredicateExpectation.Predicate() {
      @Override public boolean evaluate(Tuple tuple) {
        return !isInevitableRemainder.evaluate(tuple);
      }
    }).verify(
        actual.remainders);
  }

  protected PredicateExpectation.Predicate doesViolateConstraint(
      final ConstraintManager constraintManager) {
    Checks.checknotnull(constraintManager);
    return new PredicateExpectation.Predicate() {
      @Override public boolean evaluate(Tuple tuple) {
        try {
          return !constraintManager.check(tuple);
        } catch (UndefinedSymbol e) {
          return false;
        }
      }
    };
  }

  protected TestGenerationResult generate(
      Factors factors, int strength,
      ConstraintManager constraintManager,
      IPO2Optimizer optimizer) {
    long before = System.currentTimeMillis();
    IPO2 ipo2 = createIPO2(factors, strength, constraintManager, optimizer);
    long after = System.currentTimeMillis();
    return new TestGenerationResult(ipo2.getResult(), ipo2.getRemainders(),
        after - before);
  }

  @Test
  public void benchmark1_3$4() {
    Factors factors = buildFactors(factorsDef(3, 4));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    TestGenerationResult actual = generate(factors,
        strength, constraintManager, optimizer);

    verify(factors, strength, constraintManager, actual);
  }

  @Test
  public void benchmark2_3$13() {
    Factors factors = buildFactors(factorsDef(3, 13));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    TestGenerationResult actual = generate(factors,
        strength, constraintManager, optimizer);

    verify(factors, strength, constraintManager, actual);
  }

  @Test
  public void benchmark3_4$15_3$17_2$20() {
    Factors factors = buildFactors(factorsDef(4, 15), factorsDef(3, 17),
        factorsDef(2, 20));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    TestGenerationResult actual = generate(factors,
        strength, constraintManager, optimizer);

    verify(factors, strength, constraintManager, actual);
  }

  @Test
  public void benchmark4_4$1_3$30_2$35() {
    Factors factors = buildFactors(factorsDef(4, 1), factorsDef(3, 30),
        factorsDef(2, 35));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    TestGenerationResult actual = generate(factors,
        strength, constraintManager, optimizer);

    verify(factors, strength, constraintManager, actual);
  }

  @Test
  public void benchmark5_2$100() {
    Factors factors = buildFactors(factorsDef(2, 100));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    TestGenerationResult actual = generate(factors,
        strength, constraintManager, optimizer);

    verify(factors, strength, constraintManager, actual);
  }

  @Test
  public void benchmark6_10$20() {
    Factors factors = buildFactors(factorsDef(10, 20));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    TestGenerationResult actual = generate(factors,
        strength, constraintManager, optimizer);

    verify(factors, strength, constraintManager, actual);
  }

  static class TestGenerationResult {
    List<Tuple> testCases;
    List<Tuple> remainders;
    long        timeSpent;

    public TestGenerationResult(List<Tuple> testCases, List<Tuple> remainders,
        long timeSpent) {
      this.testCases = testCases;
      this.remainders = remainders;
      this.timeSpent = timeSpent;
    }
  }
}
