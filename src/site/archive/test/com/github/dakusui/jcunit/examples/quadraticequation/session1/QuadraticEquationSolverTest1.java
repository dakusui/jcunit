package com.github.dakusui.jcunit.examples.quadraticequation.session1;

import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * An example that tests QuadraticEquationSolver.
 * <ul>
 * <li>session 1: Initial version of QuadraticEquationSolverTest.</li>
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
    assertThat(String.format("%dx1^2+%dx1+%d=0 {x1=%f}", a, b, c, s.x1), a * s.x1 * s.x1 + b * s.x1 + c, is(0.0));
    assertThat(String.format("%dx2^2+%dx2+%d=0 {x2=%f}", a, b, c, s.x2), a * s.x2 * s.x2 + b * s.x2 + c, is(0.0));
  }
}
