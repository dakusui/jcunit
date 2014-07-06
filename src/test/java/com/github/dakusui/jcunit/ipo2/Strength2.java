package com.github.dakusui.jcunit.ipo2;

import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.generators.ipo2.Factors;
import com.github.dakusui.jcunit.generators.ipo2.Tuple;
import com.github.dakusui.jcunit.generators.ipo2.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.GreedyIPO2Optimizer;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;
import org.junit.Test;

import java.util.List;

public class Strength2 extends IPO2Test {
  int strength = 2;

  @Test
  public void test_001() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11"))
        .add(factor("F2", "L21")).build();
    ConstraintManager constraintManager = new NullConstraintManager();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<Tuple> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

  @Test
  public void test_002() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11"))
        .add(factor("F2", "L21", "L22")).build();
    ConstraintManager constraintManager = new NullConstraintManager();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<Tuple> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

  @Test
  public void test_003() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21", "L22")).build();
    ConstraintManager constraintManager = new NullConstraintManager();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<Tuple> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

  @Test
  public void test_004() {
    // This shouldn't create 2 test cases. 1 is enough
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11"))
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31")).build();
    ConstraintManager constraintManager = new NullConstraintManager();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<Tuple> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

  @Test
  public void test_005() {
    // "Don't care" is found in generated test cases.
    // "Test case that lacks F3 is generated.
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31")).build();
    ConstraintManager constraintManager = new NullConstraintManager();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<Tuple> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

  @Test
  public void test_006() {
    // "Don't care" is found in generated test cases.
    // "Test case that lacks F3 is generated.
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11"))
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31", "F32")).build();
    ConstraintManager constraintManager = new NullConstraintManager();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<Tuple> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

  @Test
  public void test_007() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21", "L22"))
        .add(factor("F3", "L31", "L32")).build();
    ConstraintManager constraintManager = new NullConstraintManager();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<Tuple> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

}
