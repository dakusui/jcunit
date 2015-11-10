package com.github.dakusui.jcunit.tests.ipo2;

import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.IPO2;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.optimizers.IPO2Optimizer;
import org.junit.Test;

public class Strength3Test extends IPO2Test {
  int strength = 3;

  @Test
  public void test_001() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11"))
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31"))
        .add(factor("F4", "L41")).build();
    ConstraintChecker constraintChecker = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = createIPO2(factors,
        strength, constraintChecker, optimizer);
    verify(strength, factors, constraintChecker, ipo.getResult(), ipo.getRemainders());
  }

  @Test
  public void test_002() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11"))
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31"))
        .add(factor("F4", "L41", "L42")).build();
    ConstraintChecker constraintChecker = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = createIPO2(factors,
        strength, constraintChecker, optimizer);
    verify(strength, factors, constraintChecker, ipo.getResult(), ipo.getRemainders());
  }

  @Test
  public void test_003() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31"))
        .add(factor("F4", "L41", "L42")).build();
    ConstraintChecker constraintChecker = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = createIPO2(factors,
        strength, constraintChecker, optimizer);
    verify(strength, factors, constraintChecker, ipo.getResult(), ipo.getRemainders());
  }

  @Test
  public void test_004() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21", "L22"))
        .add(factor("F3", "L31"))
        .add(factor("F4", "L41", "L42")).build();
    ConstraintChecker constraintChecker = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = createIPO2(factors,
        strength, constraintChecker, optimizer);
    verify(strength, factors, constraintChecker, ipo.getResult(), ipo.getRemainders());
  }

  @Test
  public void test_005() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31", "32"))
        .add(factor("F4", "L41", "L42")).build();
    ConstraintChecker constraintChecker = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = createIPO2(factors,
        strength, constraintChecker, optimizer);
    verify(strength, factors, constraintChecker, ipo.getResult(), ipo.getRemainders());
  }

  @Test
  public void test_006() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21", "L22"))
        .add(factor("F3", "L31", "L32"))
        .add(factor("F4", "L41", "L42")).build();
    ConstraintChecker constraintChecker = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = createIPO2(factors,
        strength, constraintChecker, optimizer);
    verify(strength, factors, constraintChecker, ipo.getResult(), ipo.getRemainders());
  }
}
