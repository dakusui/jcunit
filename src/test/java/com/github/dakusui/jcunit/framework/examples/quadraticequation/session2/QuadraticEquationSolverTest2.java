package com.github.dakusui.jcunit.framework.examples.quadraticequation.session2;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.framework.examples.quadraticequation.session1.QuadraticEquationSolver;
import com.github.dakusui.jcunit.generators.IPO2TupleGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import static org.junit.Assert.assertThat;

@RunWith(JCUnit.class)
@TupleGeneration(
    generator = @Generator(
        value = IPO2TupleGenerator.class,
        params = {
            @Param("2")
        }))
public class QuadraticEquationSolverTest2 {
  @FactorField
  public int a;
  @FactorField
  public int b;
  @FactorField
  public int c;

  public static boolean aIsNonZero(QuadraticEquationSolverTest2 testCase) {
    return testCase.a != 0;
  }

  public static boolean discriminantIsNonNegative(
      QuadraticEquationSolverTest2 testCase) {
    int a = testCase.a;
    int b = testCase.b;
    int c = testCase.c;
    return b * b - 4 * c * a >= 0;
  }

  public static boolean coefficientsAreValid(
      QuadraticEquationSolverTest2 testCase) {
    return Math.abs(testCase.a) <= 100 || Math.abs(testCase.b) <= 100
        || Math.abs(testCase.c) <= 100;
  }

  @Test
  @Given({ "aIsNonZero&&discriminantIsNonNegative&&coefficientsAreValid" })
  public void solveQuadraticEquation() {
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
  }
}
