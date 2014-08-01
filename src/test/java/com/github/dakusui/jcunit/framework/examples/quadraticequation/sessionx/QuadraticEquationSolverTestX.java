package com.github.dakusui.jcunit.framework.examples.quadraticequation.sessionx;

import com.github.dakusui.jcunit.constraint.Constraint;
import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.Generator;
import com.github.dakusui.jcunit.core.Param.Type;
import com.github.dakusui.jcunit.core.SchemafulTupleGeneration;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.rules.JCUnitDesc;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitSymbolException;
import com.github.dakusui.jcunit.framework.examples.quadraticequation.session1.QuadraticEquationSolver;
import com.github.dakusui.jcunit.generators.IPO2SchemafulTupleGenerator;
import com.github.dakusui.jcunit.core.TestExecution;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.jcunit.core.TestCaseUtils.createLabeledTestCase;
import static com.github.dakusui.jcunit.core.TestCaseUtils.factor;
import static com.github.dakusui.jcunit.core.TestCaseUtils.labels;
import static com.github.dakusui.jcunit.core.TestCaseUtils.newTestCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(JCUnit.class)
@SchemafulTupleGeneration(
		generator = @Generator(
				value = IPO2SchemafulTupleGenerator.class,
				params = {
						@Param(type = Type.Int, array = false, value = {"2"})
				}),
		constraint = @Constraint(
				value = QuadraticEquationSolverTestX.CM.class,
				params = {
				}))
@TestExecution(include = {0,1,2})
public class QuadraticEquationSolverTestX {
  @Rule
  public TestName   name = new TestName();
  @Rule
  public JCUnitDesc desc = new JCUnitDesc();
  @FactorField(intLevels = { 0, 1, 2, -1, -2, 100, -100, 10000, -10000 })
  public int a;
  @FactorField(intLevels = { 0, 1, 2, -1, -2, 100, -100, 10000, -10000 })
  public int b;
  @FactorField(intLevels = { 0, 1, 2, -1, -2, 100, -100, 10000, -10000 })
  public int c;

  @SuppressWarnings("unchecked")
  @JCUnit.Parameters
  public static Iterable<LabeledTestCase> parameters() {
    return Arrays.asList(
        createLabeledTestCase(labels("double root"),
            newTestCase(factor("a", 1), factor("b", 2), factor("c", 1))
        ));
  }

  @Test
  public void test() {
    System.out.println(String
        .format("desc=(%s,%s,%s)", desc.getTestName(), desc.getTestCaseType(),
            desc.getLabels()));
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
    List<Serializable> labels = desc.getLabels();
    if (labels.contains("a=0")) {
      assertEquals(null, s);
    } else if (labels.contains("b*b-4ca<0")) {
      assertEquals(null, s);
    } else if (labels.contains("nonsense 1=0")) {
      assertEquals(null, s);
    } else {
      assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
          a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
      assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
          a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
    }
  }

  public static class CM extends ConstraintManagerBase {
    @Override
    public boolean check(Tuple tuple) throws JCUnitSymbolException {
      if (!tuple.containsKey("a") || !tuple.containsKey("b") || !tuple
          .containsKey("c")) {
        throw new JCUnitSymbolException();
      }
      int a = (Integer) tuple.get("a");
      int b = (Integer) tuple.get("b");
      int c = (Integer) tuple.get("c");
      return a != 0 && b * b - 4 * c * a >= 0;
    }

    @Override
    public List<LabeledTestCase> getViolations() {
      List<LabeledTestCase> ret = new LinkedList<LabeledTestCase>();
      ret.add(createLabeledTestCase(labels("a=0"), createTestCase(0, 1, 1)));
      ret.add(createLabeledTestCase(labels("b*b-4ca<0"), createTestCase(100, 1, 100)));
      ret.add(createLabeledTestCase(labels("nonsense 1=0"), createTestCase(0, 0, 1)));
      return ret;
    }

    private Tuple createTestCase(int a, int b, int c) {
			return new Tuple.Builder().put("a", a).put("b", b).put("c", c).build();
		}
	}
}
