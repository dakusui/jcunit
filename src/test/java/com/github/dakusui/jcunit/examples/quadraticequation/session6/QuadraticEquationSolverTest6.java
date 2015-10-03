package com.github.dakusui.jcunit.examples.quadraticequation.session6;

import com.github.dakusui.jcunit.constraint.constraintmanagers.TypedConstraintManager;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.examples.quadraticequation.session4.QuadraticEquationSolver;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertThat;

/**
 * An example that tests QuadraticEquationSolver.
 * <ul>
 * <li>session 1: Initial version of QuadraticEquationSolverTest.</li>
 * <li>session 2: Exclude 'invalid' test cases.</li>
 * <li>session 3: Exclude 'too big' coefficients.</li>
 * <li>session 4: Implement parameter validation in SUT and test it.</li>
 * <li>session 5: Use constraint manager #1: First step.</li>
 * <li>session 6: Use constraint manager #2: Negative tests.</li>
 * </ul>
 */
@RunWith(JCUnit.class)
@TupleGeneration(
    constraint = @Constraint(
        value = QuadraticEquationSolverTest6.CM.class,
        params = { }))
public class QuadraticEquationSolverTest6 {
  /**
   * Constraint manager.
   */
  public static class CM extends
      TypedConstraintManager<QuadraticEquationSolverTest6> {
    @Override
    public boolean check(QuadraticEquationSolverTest6 obj, Tuple testCase)
        throws UndefinedSymbol {
      Checks.checksymbols(testCase, "a", "b", "c");
      return obj.discriminantIsNonNegative() && obj.aIsNonZero();
    }

    @Override
    protected List<QuadraticEquationSolverTest6> getViolationTestObjects() {
      List<QuadraticEquationSolverTest6> ret = new LinkedList<QuadraticEquationSolverTest6>();
      ret.add(create(0, 1, 1));
      ret.add(create(100, 1, 100));
      ret.add(create(0, 0, 1));
      ret.add(create(101, 100, 1));
      ret.add(create(1, 100, -101));
      return ret;
    }

    private static QuadraticEquationSolverTest6 create(int a, int b, int c) {
      QuadraticEquationSolverTest6 ret = new QuadraticEquationSolverTest6();
      ret.a = a;
      ret.b = b;
      ret.c = c;
      return ret;
    }
  }

  public static final int runCount     = 63; // 58 + 5
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
   * <p/>
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
  }

  @Test
  @When({ "aIsNonZero&&discriminantIsNonNegative&&coefficientsAreValid" })
  public void whenSolveEquation$thenSolved() {
    System.out.println(String.format("(a,b,c,b*b,-4*c*a,discriminant)=(%d,%d,%d,%d,%d,%d)", a, b, c, b * b, -4 * c * a, b * b - 4 * c * a));
    System.out.println(this.coefficientsAreValid());
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
  }
}
