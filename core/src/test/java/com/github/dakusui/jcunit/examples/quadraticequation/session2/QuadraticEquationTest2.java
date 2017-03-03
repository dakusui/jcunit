package com.github.dakusui.jcunit.examples.quadraticequation.session2;

import com.github.dakusui.jcunit.runners.standard.annotations.Condition;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.Given;
import com.github.dakusui.jcunit.examples.quadraticequation.session1.QuadraticEquationSolver;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import static org.junit.Assert.assertThat;

/**
 * An example that tests QuadraticEquationSolver.
 * <ul>
 *   <li>session 1: Initial version of QuadraticEquationSolverTest.</li>
 *   <li>session 2: Exclude 'invalid' test cases.</li>
 * </ul>
 */
@RunWith(JCUnit.class)
public class QuadraticEquationTest2 {
  public static final int runCount     = 37;
  public static final int failureCount = 12;
  public static final int ignoreCount  = 16;

  @FactorField
  public int a;
  @FactorField
  public int b;
  @FactorField
  public int c;

  @Condition
  public boolean aIsNonZero() {
    return a != 0;
  }

  @Condition
  public boolean discriminantIsNonNegative() {
    return b * b - 4 * c * a >= 0;
  }

  @Test
  @Given({ "aIsNonZero&&discriminantIsNonNegative" })
  public void solveEquation() {
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
  }
}
