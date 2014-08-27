package com.github.dakusui.jcunit.examples.quadraticequation.session1;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.tests.generators.IPO2TupleGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JCUnit.class)
@TupleGeneration(
    generator = @Generator(
        value = IPO2TupleGenerator.class,
        params = {
            @Param("2")
        }))
public class QuadraticEquationSolverTest1 {
  @FactorField
  public int a;
  @FactorField
  public int b;
  @FactorField
  public int c;

  @Test
  public void solveQuadraticEquation() {
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
    assertThat(a * s.x1 * s.x1 + b * s.x1 + c, is(0.0));
    assertThat(a * s.x2 * s.x2 + b * s.x2 + c, is(0.0));
  }
}
