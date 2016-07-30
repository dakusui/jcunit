package com.github.dakusui.jcunit.tests.plugins.caengines;


import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.coverage.CombinatorialMetrics;
import com.github.dakusui.jcunit.coverage.Metrics;
import com.github.dakusui.jcunit.coverage.Report;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.plugins.caengines.AetgCoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class AetgCoveringArrayEngineTest {
  /**
   * Tests if AetgCoveringArrayGenerator creates a test suite.
   * Currently this test only checks the size.
   */
  @Test(timeout = 30000)
  public void aetgTestSuiteBuildingNegativeTestGenerationDisabled() {
    int strength = 2;
    Factors factors = new Factors.Builder()
        .add(new Factor.Builder("factor1").addLevel(1).addLevel(2).addLevel(3).build())
        .add(new Factor.Builder("factor2").addLevel(1).addLevel(2).addLevel(3).build())
        .add(new Factor.Builder("factor3").addLevel(1).addLevel(2).addLevel(3).build())
        .build();

    TestSuite.Builder testSuiteBuilder = new TestSuite.Builder(new AetgCoveringArrayEngine(strength, 0))
        .addFactors(
            factors)
        .disableNegativeTests();
    TestSuite testSuite =  testSuiteBuilder.build();
    printReport(factors, testSuite, strength);
    assertEquals(10, testSuite.size());
  }

  @Test(timeout = 30000)
  public void aetgTestSuiteBuildingNegativeTestGenerationDisabled$includingFactorWhichHasOnlyOneLevel() {
    int strength = 2;
    Factors factors = new Factors.Builder()
        .add(new Factor.Builder("factor1").addLevel(1).build())
        .add(new Factor.Builder("factor2").addLevel(1).addLevel(2).addLevel(3).build())
        .add(new Factor.Builder("factor3").addLevel(1).addLevel(2).addLevel(3).build())
        .build();

    TestSuite.Builder testSuiteBuilder = new TestSuite.Builder(new AetgCoveringArrayEngine(strength, 0))
        .addFactors(
            factors)
        .disableNegativeTests();
    TestSuite testSuite =  testSuiteBuilder.build();
    printReport(factors, testSuite, strength);
    assertEquals(9, testSuite.size());
  }


  @Test(timeout = 30000)
  public void aetgTestSuiteBuildingNegativeTestGenerationDisabled$fromOnlyOneFactor() {
    int strength = 1;
    Factors factors = new Factors.Builder()
        .add(new Factor.Builder("factor1").addLevel(1).addLevel(2).addLevel(3).build())
        .build();

    TestSuite.Builder testSuiteBuilder = new TestSuite.Builder(new AetgCoveringArrayEngine(strength, 0))
        .addFactors(
            factors)
        .disableNegativeTests();
    TestSuite testSuite =  testSuiteBuilder.build();
    printReport(factors, testSuite, strength);
    assertEquals(3, testSuite.size());
  }


  @Test(timeout = 30000)
  public void aetgTestSuiteBuildingNegativeTestGenerationEnabled$onlyTwoFactors() {
    int strength = 2;
    Factors factors = new Factors.Builder()
        .add(new Factor.Builder("factor1").addLevel(1).addLevel(2).addLevel(3).build())
        .add(new Factor.Builder("factor2").addLevel(1).addLevel(2).addLevel(3).build())
        .build();

    TestSuite.Builder testSuiteBuilder = new TestSuite.Builder(new AetgCoveringArrayEngine(strength, 0))
        .addFactors(
            factors)
        .enableNegativeTests();
    TestSuite testSuite =  testSuiteBuilder.build();
    printReport(factors, testSuite, strength);
    assertEquals(9, testSuite.size());
  }

  @Test(timeout = 30000)
  public void aetgTestSuiteBuildingNegativeTestGenerationDisabled$simplestConstraintPresent() {
    int strength = 2;
    Factors factors = new Factors.Builder()
        .add(new Factor.Builder("factor1").addLevel(1).addLevel(2).addLevel(3).addLevel(4).build())
        .add(new Factor.Builder("factor2").addLevel(1).addLevel(2).addLevel(3).build())
        .add(new Factor.Builder("factor3").addLevel(1).addLevel(2).addLevel(3).build())
        .build();

    TestSuite.Builder testSuiteBuilder = new TestSuite.Builder(new AetgCoveringArrayEngine(strength, 0))
        .addFactors(
            factors)
        .addConstraint(new TestSuite.Predicate("factor1 mustn't be 4", "factor1") {
          @Override
          public boolean apply(Tuple in) {
            return !Integer.class.cast(in.get("factor1")).equals(4);
          }
        })
        .disableNegativeTests();
    TestSuite testSuite =  testSuiteBuilder.build();
    printReport(factors, testSuite, strength);
    assertEquals(10, testSuite.size());
  }

  @Test(timeout = 30000)
  public void aetgTestSuiteBuildingNegativeTestGenerationEnabled$twoFactorConstraintPresent() {
    int strength = 2;
    Factors factors = new Factors.Builder()
        .add(new Factor.Builder("factor1").addLevel(1).addLevel(2).addLevel(3).build())
        .add(new Factor.Builder("factor2").addLevel(1).addLevel(2).addLevel(3).build())
        .add(new Factor.Builder("factor3").addLevel(1).addLevel(2).addLevel(3).build())
        .build();

    TestSuite.Builder testSuiteBuilder = new TestSuite.Builder(new AetgCoveringArrayEngine(strength, 0))
        .addFactors(
            factors)
        .addConstraint(new TestSuite.Predicate("factor1 + factor2 must be less than 5", "factor1", "factor2") {
          @Override
          public boolean apply(Tuple in) {
            int factor1 = Integer.class.cast(in.get("factor2"));
            int factor2 = Integer.class.cast(in.get("factor3"));
            return factor1 + factor2 < 5;
          }
        })
        .enableNegativeTests();
    TestSuite testSuite =  testSuiteBuilder.build();
    printReport(factors, testSuite, strength);
    assertEquals(10, testSuite.size());
  }

  @Test(timeout = 30000)
  public void aetgTestSuiteBuildingNegativeTestGenerationEnabled$twoTwoFactorConstraintPresent() {
    int strength = 2;
    Factors factors = new Factors.Builder()
        .add(new Factor.Builder("factor1").addLevel(1).addLevel(2).addLevel(3).build())
        .add(new Factor.Builder("factor2").addLevel(1).addLevel(2).addLevel(3).build())
        .add(new Factor.Builder("factor3").addLevel(1).addLevel(2).addLevel(3).build())
        .build();

    TestSuite.Builder testSuiteBuilder = new TestSuite.Builder(new AetgCoveringArrayEngine(strength, 0))
        .addFactors(
            factors)
        .addConstraint(new TestSuite.Predicate("factor1 + factor2 must be less than 5", "factor1", "factor2") {
          @Override
          public boolean apply(Tuple in) {
            int factor1 = Integer.class.cast(in.get("factor2"));
            int factor2 = Integer.class.cast(in.get("factor3"));
            return factor1 + factor2 < 5;
          }
        })
        .addConstraint(new TestSuite.Predicate("factor2 + factor 3 must be more than 3", "factor2", "factor3") {
          @Override
          public boolean apply(Tuple in) {
            int factor2 = Integer.class.cast(in.get("factor2"));
            int factor3 = Integer.class.cast(in.get("factor3"));
            return factor2 + factor3 > 3;
          }
        })
        .enableNegativeTests();
    TestSuite testSuite =  testSuiteBuilder.build();
    printReport(factors, testSuite, strength);
    assertEquals(10, testSuite.size());
  }

  private void printReport(Factors factors, TestSuite testSuite, int strength) {
    System.out.println("Generated test suite");
    for (TestCase each : testSuite) {
      System.out.println("  " + each.getTuple());
    }
    Metrics<Tuple> metrics = new CombinatorialMetrics(
        factors,
        ConstraintChecker.DEFAULT_CONSTRAINT_CHECKER,
        strength
    );
    metrics.process(
        Utils.transform(testSuite, new Utils.Form<TestCase, Tuple>() {
          @Override
          public Tuple apply(TestCase in) {
            return in.getTuple();
          }
        })
    );
    new Report.Printer(System.out).submit(metrics);
  }

}
