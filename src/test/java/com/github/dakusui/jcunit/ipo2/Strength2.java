package com.github.dakusui.jcunit.ipo2;

import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.core.ValueTuple;
import com.github.dakusui.jcunit.generators.ipo2.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.GreedyIPO2Optimizer;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;
import org.junit.Test;

import java.util.List;

/**
 * Created by hiroshi on 7/3/14.
 */
public class Strength2 extends IPO2Test {
  int strength = 2;
  @Test
  public void test_001() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11"))
        .add(factor("F2", "L21")).build();
    ConstraintManager<String, Object> constraintManager = new NullConstraintManager<String, Object>();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<ValueTuple<String, Object>> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

  @Test
  public void test_002() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11"))
        .add(factor("F2", "L21", "L22")).build();
    ConstraintManager<String, Object> constraintManager = new NullConstraintManager<String, Object>();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<ValueTuple<String, Object>> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

  @Test
  public void test_003() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21", "L22")).build();
    ConstraintManager<String, Object> constraintManager = new NullConstraintManager<String, Object>();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<ValueTuple<String, Object>> testcases = generate(factors,
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
    ConstraintManager<String, Object> constraintManager = new NullConstraintManager<String, Object>();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<ValueTuple<String, Object>> testcases = generate(factors,
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
    ConstraintManager<String, Object> constraintManager = new NullConstraintManager<String, Object>();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<ValueTuple<String, Object>> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

  @Test
  public void test_006() {
    // "Don't care" is found in generated test cases.
    // "Test case that lacks F3 is generated.
    Factors factors = new Factors.Builder().add(factor("F1", "L11"))
        .add(factor("F2", "L21")).add(factor("F3", "L31", "F32")).build();
    ConstraintManager<String, Object> constraintManager = new NullConstraintManager<String, Object>();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<ValueTuple<String, Object>> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

}
