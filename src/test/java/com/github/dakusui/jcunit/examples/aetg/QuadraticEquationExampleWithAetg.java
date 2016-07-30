package com.github.dakusui.jcunit.examples.aetg;

import com.github.dakusui.jcunit.examples.quadraticequation.session6.QuadraticEquationSolver;
import com.github.dakusui.jcunit.plugins.caengines.AetgCoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintCheckerImpl;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.jcunit.runners.standard.rules.TestDescription;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import java.io.PrintStream;

import static org.junit.Assert.assertThat;


/**
 * This class is created from {@code com.github.dakusui.jcunit.examples.quadraticequation.session8.QuadraticEquationSolverTest8}.
 */
@RunWith(JCUnit.class)
@GenerateCoveringArrayWith(
    engine = @Generator(value = AetgCoveringArrayEngine.class),
    checker = @Checker(value = SmartConstraintCheckerImpl.class),
    reporters = {})
public class QuadraticEquationExampleWithAetg {
  public static PrintStream ps1 = System.out;
  public static PrintStream ps2 = System.err;

  public static final int runCount     = 33 * 2; // (20 (regular) + 8 (violation)) * 2; One for solve, the other for print.
  public static final int failureCount = 0;
  public static final int ignoreCount  = 0;

  @Rule
  public TestDescription testDescription = new TestDescription();

  @FactorField
  public int a;
  @FactorField
  public int b;
  @FactorField
  public int c;

  @Uses({ "a" })
  @Condition(constraint = true)
  public boolean aIsNonZero() {
    return this.a != 0;
  }

  @Uses({ "a", "b", "c" })
  @Condition(constraint = true)
  public boolean discriminantIsNonNegative() {
    int a = this.a;
    int b = this.b;
    int c = this.c;
    return b * b - 4 * c * a >= 0;
  }

  @Uses({ "a", "b", "c" })
  @Condition(constraint = true)
  public boolean coefficientsAreValid() {
    return
        -100 <= a && a <= 100 &&
            -100 <= b && b <= 100 &&
            -100 <= c && c <= 100;
  }

  @Test(expected = IllegalArgumentException.class)
  @When({ "!aIsNonZero" })
  public void solveEquation1$thenThrowIllegalArgumentException() {
    new QuadraticEquationSolver(
        a,
        b,
        c).solve();
  }

  @Test(expected = IllegalArgumentException.class)
  @When({ "!discriminantIsNonNegative" })
  public void solveEquation2$thenThrowIllegalArgumentException() {
    new QuadraticEquationSolver(
        a,
        b,
        c).solve();
  }

  @Test(expected = IllegalArgumentException.class)
  @When({ "!coefficientsAreValid" })
  public void solveEquation3$thenThrowIllegalArgumentException() {
    new QuadraticEquationSolver(
        a,
        b,
        c).solve();
  }

  @Test
  @When({ "*" })
  public void solveEquation$thenSolved() {
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
  }

  @Test
  @When({ "aIsNonZero&&discriminantIsNonNegative&&coefficientsAreValid" })
  public void printEquationToStdOut() {
    ps1.println(String.format("Regular: (a,b,c)=(%d,%d,%d)", a, b, c));
  }

  @Test
  @When({ "!aIsNonZero", "!discriminantIsNonNegative", "!coefficientsAreValid" })
  public void printEquationToStdErr() {
    ps2.println(String.format("Invalid: (a,b,c)=(%d,%d,%d)", a, b, c));
  }

}
