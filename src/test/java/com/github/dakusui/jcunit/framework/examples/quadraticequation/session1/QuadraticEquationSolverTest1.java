package com.github.dakusui.jcunit.framework.examples.quadraticequation.session1;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.generators.IPO2SchemafulTupleGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(JCUnit.class)
@SchemafulTupleGeneration(
		generator = @Generator(
				value = IPO2SchemafulTupleGenerator.class,
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
	public void test() {
		QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
				c).solve();
		assertEquals(0.0, a * s.x1 * s.x1 + b * s.x1 + c);
		assertEquals(0.0, a * s.x2 * s.x2 + b * s.x2 + c);
	}
}
