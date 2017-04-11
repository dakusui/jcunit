package com.github.dakusui.jcunit.examples.quadraticequation.session3;

import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.Given;
import com.github.dakusui.jcunit.examples.quadraticequation.session1.QuadraticEquationSolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import static org.junit.Assert.assertThat;

/**
 * An example that tests QuadraticEquationSolver.
 * <ul>
 * <li>session 1: Initial version of QuadraticEquationSolverTest.</li>
 * <li>session 2: Exclude 'invalid' test cases.</li>
 * <li>session 3: Exclude 'too big' coefficients.</li>
 * </ul>
 */
@RunWith(JCUnit.class)
public class QuadraticEquationSolverTest3 {
  public static final int runCount     = 14;
  public static final int failureCount = 0;
  public static final int ignoreCount  = 39;

  @FactorField
  public int a;
  @FactorField
  public int b;
  @FactorField
  public int c;

  @Condition
  public boolean aIsNonZero() {
    return this.a != 0;
  }

  @Condition
  public boolean discriminantIsNonNegative() {
    int a = this.a;
    int b = this.b;
    int c = this.c;
    return b * b - 4 * c * a >= 0;
  }

  @Condition
  public boolean coefficientsAreValid() {
    return
        -100 <= a && a <= 100 &&
            -100 <= b && b <= 100 &&
            -100 <= c && c <= 100;
  }

  @Test
  @Given({ "aIsNonZero&&discriminantIsNonNegative&&coefficientsAreValid" })
  public void solveEquation$thenSolved() {
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
  }
}
