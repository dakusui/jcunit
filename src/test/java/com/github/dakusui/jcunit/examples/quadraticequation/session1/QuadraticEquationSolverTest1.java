package com.github.dakusui.jcunit.examples.quadraticequation.session1;

import com.github.dakusui.jcunit.standardrunner.annotations.FactorField;
import com.github.dakusui.jcunit.standardrunner.JCUnit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * An example that tests QuadraticEquationSolver.
 * <ul>
 *   <li>session 1: Initial version of QuadraticEquationSolverTest.</li>
 * </ul>
 */
@RunWith(JCUnit.class)
public class QuadraticEquationSolverTest1 {
  public static final int runCount     = 53;
  public static final int failureCount = 39;
  public static final int ignoreCount  = 0;

  @FactorField
  public int a;
  @FactorField
  public int b;
  @FactorField
  public int c;

  @Test
  public void solveEquation() {
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
    assertThat(a * s.x1 * s.x1 + b * s.x1 + c, is(0.0));
    assertThat(a * s.x2 * s.x2 + b * s.x2 + c, is(0.0));
  }
}
