package com.github.dakusui.jcunit.examples.quadraticequation.session7;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.examples.quadraticequation.session4.QuadraticEquationSolver;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.Checker;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.GenerateCoveringArrayWith;
import com.github.dakusui.jcunit.runners.standard.annotations.When;
import com.github.dakusui.jcunit.runners.standard.rules.TestDescription;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.PrintStream;

/**
 * An example that tests QuadraticEquationSolver.
 * <ul>
 * <li>session 1: Initial version of QuadraticEquationSolverTest.</li>
 * <li>session 2: Exclude 'invalid' test cases.</li>
 * <li>session 3: Exclude 'too big' coefficients.</li>
 * <li>session 4: Implement parameter validation in SUT and test it.</li>
 * <li>session 5: Use constraint checker #1: First step.</li>
 * <li>session 6: Use constraint checker #2: Negative tests.</li>
 * </ul>
 */
@RunWith(JCUnit.class)
@GenerateCoveringArrayWith(
    checker = @Checker(
        value = QuadraticEquationSolverTest7.CM.class,
        args = {}))
public class QuadraticEquationSolverTest7 {
  public static PrintStream ps = UTUtils.DUMMY_PRINTSTREAM;

  @Rule
  public TestDescription testDescription = new TestDescription();

  public static class CM extends SmartConstraintChecker<QuadraticEquationConstraint> {
    @Override
    protected Class<QuadraticEquationConstraint> getConstraintClass() {
      return QuadraticEquationConstraint.class;
    }
  }

  @SuppressWarnings("unused")
  public enum QuadraticEquationConstraint implements Constraint {
    A_IS_NON_ZERO("aIsNonZero") {
      @Override
      protected boolean check(QuadraticEquationSolverTest7 testObject) throws UndefinedSymbol {
        return testObject.a != 0;
      }
    },
    DISCRIMINANT_NON_NEGATIVE("discriminantIsNonNegative") {
      @Override
      protected boolean check(QuadraticEquationSolverTest7 testObject) throws UndefinedSymbol {
        return testObject.b * testObject.b - 4 * testObject.c * testObject.a >= 0;
      }
    },
    A_IS_IN_RANGE("coefficientsAreValid") {
      @Override
      protected boolean check(QuadraticEquationSolverTest7 testObject) throws UndefinedSymbol {
        return -100 <= testObject.a && testObject.a <= 100;
      }
    },
    B_IS_IN_RANGE("coefficientsAreValid") {
      @Override
      protected boolean check(QuadraticEquationSolverTest7 testObject) throws UndefinedSymbol {
        return -100 <= testObject.b && testObject.b <= 100;
      }
    },
    C_IS_IN_RANGE("coefficientsAreValid") {
      @Override
      protected boolean check(QuadraticEquationSolverTest7 testObject) throws UndefinedSymbol {
        return -100 <= testObject.c && testObject.c <= 100;
      }
    },;

    private final String tag;

    QuadraticEquationConstraint(String tag) {
      this.tag = tag;
    }

    @Override
    public boolean check(Tuple tuple) throws UndefinedSymbol {
      return check(TestCaseUtils.toTestObject(QuadraticEquationSolverTest7.class, tuple));
    }

    @Override
    public String tag() {
      return this.tag;
    }

    abstract protected boolean check(QuadraticEquationSolverTest7 testObject) throws UndefinedSymbol;

  }

  @FactorField
  public int a;
  @FactorField
  public int b;
  @FactorField
  public int c;

  @Test(expected = IllegalArgumentException.class)
  @When({ "#aIsNonZero", "#discriminantIsNonNegative", "#coefficientsAreValid"})
  public void solveEquation$thenThrowIllegalArgumentException() {
    new QuadraticEquationSolver(
        a,
        b,
        c).solve();
  }
/*
  @Test
  @When({ "#aIsNonZero&&#discriminantIsNonNegative&&#coefficientsAreValid" })
  public void solveEquation$thenSolved() {
    System.out.println("-->" + this.testDescription.getTestName() + "<--");
    System.out.println(String.format("(a,b,c)=(%d,%d,%d)", a, b, c));
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
  }
  */
}
