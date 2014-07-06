package com.github.dakusui.jcunit.ipo2;

import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.generators.ipo2.Factors;
import com.github.dakusui.jcunit.generators.ipo2.Tuple;
import com.github.dakusui.jcunit.generators.ipo2.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.GreedyIPO2Optimizer;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;
import org.junit.Test;

import java.util.List;

public class Strength3 extends IPO2Test {
  int strength = 3;

  @Test
  public void test_001() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11"))
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31"))
        .add(factor("F4", "L41")).build();
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
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31"))
        .add(factor("F4", "L41", "L42")).build();
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
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31"))
        .add(factor("F4", "L41", "L42")).build();
    ConstraintManager constraintManager = new NullConstraintManager();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<Tuple> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

  @Test
  public void test_004() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21", "L22"))
        .add(factor("F3", "L31"))
        .add(factor("F4", "L41", "L42")).build();
    ConstraintManager constraintManager = new NullConstraintManager();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<Tuple> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

  @Test
  public void test_005() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21"))
        .add(factor("F3", "L31", "32"))
        .add(factor("F4", "L41", "L42")).build();
    ConstraintManager constraintManager = new NullConstraintManager();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<Tuple> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }

  @Test
  public void test_006() {
    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L12"))
        .add(factor("F2", "L21", "L22"))
        .add(factor("F3", "L31", "L32"))
        .add(factor("F4", "L41", "L42")).build();
    ConstraintManager constraintManager = new NullConstraintManager();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<Tuple> testcases = generate(factors,
        strength, constraintManager, optimizer);

    verify(testcases, strength, factors);
  }
}
