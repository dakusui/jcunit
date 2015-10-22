package com.github.dakusui.jcunit.examples.quadraticequation.session5;

import com.github.dakusui.jcunit.standardrunner.annotations.Constraint;
import com.github.dakusui.jcunit.standardrunner.annotations.FactorField;
import com.github.dakusui.jcunit.standardrunner.annotations.TupleGeneration;
import com.github.dakusui.jcunit.standardrunner.annotations.When;
import com.github.dakusui.jcunit.plugins.constraintmanagers.TypedConstraintManager;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.examples.quadraticequation.session4.QuadraticEquationSolver;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.standardrunner.JCUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import java.io.PrintStream;

import static org.junit.Assert.assertThat;

/**
 * An example that tests QuadraticEquationSolver.
 * <ul>
 * <li>session 1: Initial version of QuadraticEquationSolverTest.</li>
 * <li>session 2: Exclude 'invalid' test cases.</li>
 * <li>session 3: Exclude 'too big' coefficients.</li>
 * <li>session 4: Implement parameter validation in SUT and test it.</li>
 * <li>session 5: Use constraint manager #1: First step.</li>
 * </ul>
 */
@RunWith(JCUnit.class)
@TupleGeneration(
    constraint = @Constraint(
        value = QuadraticEquationSolverTest5.CM.class,
        params = { }))
public class QuadraticEquationSolverTest5 {
  public static PrintStream ps = System.out;

  /**
   * Constraint manager.
   */
  public static class CM extends
      TypedConstraintManager<QuadraticEquationSolverTest5> {
    @Override
    public boolean check(QuadraticEquationSolverTest5 obj, Tuple testCase)
        throws UndefinedSymbol {
      Checks.checksymbols(testCase, "a", "b", "c");
      return obj.discriminantIsNonNegative() && obj.aIsNonZero();
    }
  }

  public static final int runCount     = 58;
  public static final int failureCount = 0;
  public static final int ignoreCount  = 0;

  @FactorField
  public int a;
  @FactorField
  public int b;
  @FactorField
  public int c;

  public boolean aIsNonZero() {
    return this.a != 0;
  }

  public boolean discriminantIsNonNegative() {
    int a = this.a;
    int b = this.b;
    int c = this.c;
    return b * b - 4 * c * a >= 0;
  }

  public boolean coefficientsAreValid() {
    return
        -100 <= a && a <= 100 &&
            -100 <= b && b <= 100 &&
            -100 <= c && c <= 100;
  }

  /**
   * Since a constraint manager CM is present, invalid test cases will not be
   * generated anymore.
   *
   * See, there is a statement {@code throw new Error();}, but no error is reported.
   * Now, this test isn't executed at all.
   */
  @Test(expected = IllegalArgumentException.class)
  @When({ "!aIsNonZero", "!discriminantIsNonNegative", "!coefficientsAreValid" })
  public void whenSolveEquation$thenIllegalArgumentExceptionWillBeThrown() {
    new QuadraticEquationSolver(
        a,
        b,
        c).solve();
    throw new Error();
  }

  @Test
  @When({ "aIsNonZero&&discriminantIsNonNegative&&coefficientsAreValid" })
  public void whenSolveEquation$thenSolved() {
    ps.println(String.format("(a,b,c,b*b,-4*c*a,discriminant)=(%d,%d,%d,%d,%d,%d)", a, b, c, b * b, -4 * c * a, b * b - 4 * c * a));
    ps.println(this.coefficientsAreValid());
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
  }
}
