package com.github.dakusui.jcunit.examples.quadraticequation.session3;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.examples.quadraticequation.session1.QuadraticEquationSolver;
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
public class QuadraticEquationSolverTest3 {
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
    return Math.abs(this.a) <= 100 || Math.abs(this.b) <= 100
        || Math.abs(this.c) <= 100;
  }

  @Test(expected = IllegalArgumentException.class)
  @Given({ "!aIsNonZero", "!discriminantIsNonNegative", "!coefficientsAreValid" })
  public void whenSolveEquation$thenIllegalArgumentExceptionWillBeThrown() {
    new QuadraticEquationSolver(
        a,
        b,
        c).solve();
  }

  @Test
  @Given({ "aIsNonZero&&discriminantIsNonNegative&&coefficientsAreValid" })
  public void whenSolveQuadraticEquation$thenSolved() {
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
  }
}
