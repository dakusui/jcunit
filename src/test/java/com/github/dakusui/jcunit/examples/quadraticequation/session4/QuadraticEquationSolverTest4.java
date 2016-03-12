package com.github.dakusui.jcunit.examples.quadraticequation.session4;

import com.github.dakusui.jcunit.runners.standard.annotations.Condition;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.When;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import java.io.PrintStream;

import static org.junit.Assert.assertThat;

/**
 * An example that tests QuadraticEquationSolver.
 * <ul>
 *   <li>session 1: Initial version of QuadraticEquationSolverTest.</li>
 *   <li>session 2: Exclude 'invalid' test cases.</li>
 *   <li>session 3: Exclude 'too big' coefficients.</li>
 *   <li>session 4: Implement parameter validation in SUT and test it.</li>
 * </ul>
 */
@RunWith(JCUnit.class)
public class QuadraticEquationSolverTest4 {
  public static PrintStream ps = System.out;

  public static final int runCount     = 53;
  public static final int failureCount = 10;
  public static final int ignoreCount  = 0;

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

  @Test(expected = IllegalArgumentException.class)
  @When({ "!aIsNonZero", "!discriminantIsNonNegative", "!coefficientsAreValid" })
  public void solveEquation$thenIllegalArgumentExceptionWillBeThrown() {
    new QuadraticEquationSolver(
        a,
        b,
        c).solve();
  }

  @Test
  @When({ "aIsNonZero&&discriminantIsNonNegative&&coefficientsAreValid" })
  public void solveEquation$thenSolved() {
    ps.println(String.format("(a,b,c,b*b,-4*c*a,discriminant)=(%d,%d,%d,%d,%d,%d)", a, b, c, b*b, -4*c*a, b*b-4*c*a));
    ps.println(this.coefficientsAreValid());
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
  }
}
