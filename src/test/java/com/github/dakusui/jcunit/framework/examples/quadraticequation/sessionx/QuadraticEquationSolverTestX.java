package com.github.dakusui.jcunit.framework.examples.quadraticequation.sessionx;

import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.rules.JCUnitDesc;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitSymbolException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.jcunit.core.TestCaseUtils.*;
import static org.junit.Assert.assertThat;

@RunWith(JCUnit.class)
/*/
@TupleGeneration(
    constraint = @Constraint(
        value = QuadraticEquationSolverTestX.CM.class,
        params = {
        }))
        /*/
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
  @CustomTestCases
  public static Iterable<LabeledTestCase> parameters() {
    return Arrays.asList(
        createLabeledTestCase(labels("double root"),
            newTestCase(factor("a", 1), factor("b", 2), factor("c", 1))),
        createLabeledTestCase(labels("linear equation"),
            newTestCase(factor("a", 0), factor("b", 200), factor("c", 1))
        ));
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
      ret.add(createLabeledTestCase(labels("b*b-4ca<0"),
          createTestCase(100, 1, 100)));
      ret.add(createLabeledTestCase(labels("nonsense 1=0"),
          createTestCase(0, 0, 1)));
      return ret;
    }

    private Tuple createTestCase(int a, int b, int c) {
      return new Tuple.Builder().put("a", a).put("b", b).put("c", c).build();
    }
  }

  //  @Precondition
  public static boolean isANonZero(QuadraticEquationSolverTestX testCase) {
    return testCase.a != 0;
  }

  public static boolean isDiscriminantNonNegative(QuadraticEquationSolverTestX test) {
    int a = test.a;
    int b = test.b;
    int c = test.c;
    return b * b - 4 * c * a >= 0;
  }

  public static boolean isValidQuadratic(QuadraticEquationSolverTestX test) {
    return isANonZero(test) && isDiscriminantNonNegative(test);
  }

  @Given({ "!isANonZero", "!isDiscriminantNonNegative" })
  @Test(expected = IllegalArgumentException.class)
  public void solveEquationThenIllegalArgumentExceptionWillBeThrown() {
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
  }

  @Given({ "isValidQuadratic" })
  @Test
  public void solveEquationThenSolutionsArePreciseEnough() {
    try {
      System.out.println(String
          .format("desc=(%s,%s)", desc.getTestName(), desc.getTestCaseType()));
      QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
          c).solve();
      assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
          a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
      assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
          a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
    } catch (IllegalArgumentException e) {
      System.err.println("*** " + this.desc.getTestCase() + " ***");
      throw e;
    }
  }
}
