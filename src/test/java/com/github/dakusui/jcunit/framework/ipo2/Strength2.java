package com.github.dakusui.jcunit.framework.ipo2;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;
import org.junit.Test;

public class Strength2 extends IPO2Test {
  protected  int strength = 2;

  @Test
  public void test_001() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11"))
        .add(factor("F2", "L21")).build();
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void test_002() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11"))
        .add(factor("F2", "L21", "L22")).build();
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void test_003() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21", "L22")).build();
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void test_004() {
    // This shouldn't create 2 test cases. 1 is enough
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11"))
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31")).build();
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void test_005() {
    // "Don't care" is found in generated test cases.
    // "Test case that lacks F3 is generated.
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31")).build();
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void test_006() {
    // "Don't care" is found in generated test cases.
    // "Test case that lacks F3 is generated.
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11"))
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31", "F32")).build();
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void test_007() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21", "L22"))
        .add(factor("F3", "L31", "L32")).build();
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void test_008() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21", "L22", "L23"))
        .add(factor("F3", "L31", "L32", "L33", "L34"))
        .add(factor("F4", "L41", "L42", "L43", "L44", "L45"))
        .add(factor("F5", "L51", "L52", "L53", "L54", "L55", "L56"))
        .add(factor("F6", "L61", "L62", "L63", "L64", "L65", "L66", "L67"))
        .build();
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void test_009() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12", "L13", "L14", "L15", "L16", "L17"))
        .add(factor("F2", "L21", "L22", "L23", "L24", "L25", "L26"))
        .add(factor("F3", "L31", "L32", "L33", "L34", "L35"))
        .add(factor("F4", "L41", "L42", "L43", "L44"))
        .add(factor("F5", "L51", "L52", "L53"))
        .add(factor("F6", "L61", "L62"))
        .build();
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }
}
