package com.github.dakusui.jcunit.examples.quadraticequation.session7;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.examples.quadraticequation.session4.QuadraticEquationSolver;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.jcunit.runners.standard.rules.TestDescription;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import java.io.PrintStream;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * An example that tests QuadraticEquationSolver.
 * <ul>
 * <li>session 1: Initial version of QuadraticEquationSolverTest.</li>
 * <li>session 2: Exclude 'invalid' test cases.</li>
 * <li>session 3: Exclude 'too big' coefficients.</li>
 * <li>session 4: Implement parameter validation in SUT and test it.</li>
 * <li>session 5: Use constraint checker #1: First step.</li>
 * <li>session 6: Use constraint checker #2: Negative tests.</li>
 * <li>session 7: Use constraint checker #3: Automatically generated negative tests.</li>
 * </ul>
 */
@RunWith(JCUnit.class)
@GenerateCoveringArrayWith(
    checker = @Checker(
        value = SmartConstraintChecker.class,
        args = { @Value("com.github.dakusui.jcunit.examples.quadraticequation.session7.QuadraticEquationSolverTest7$QuadraticEquationConstraint")}))
public class QuadraticEquationSolverTest7 {
  public static       PrintStream ps           = System.out;
  public static final int         runCount     = 28; // 20 (regular) + 8 (violation)
  public static final int         failureCount = 0;
  public static final int         ignoreCount  = 0;

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

  @Rule
  public TestDescription testDescription = new TestDescription();

  @FactorField
  public int a;
  @FactorField
  public int b;
  @FactorField
  public int c;

  @Test(expected = IllegalArgumentException.class)
  //  @When({ "!#aIsNonZero", "!#discriminantIsNonNegative", "!#coefficientsAreValid" })
  @When({ "!#aIsNonZero" })
  public void solveEquation1$thenThrowIllegalArgumentException() {
    ps.println(String.format("(a,b,c)=(%d,%d,%d)", a, b, c));
    new QuadraticEquationSolver(
        a,
        b,
        c).solve();
  }

  @Test(expected = IllegalArgumentException.class)
  @When({ "!#discriminantIsNonNegative" })
  public void solveEquation2$thenThrowIllegalArgumentException() {
    ps.println(String.format("(a,b,c)=(%d,%d,%d)", a, b, c));
    new QuadraticEquationSolver(
        a,
        b,
        c).solve();
  }

  @Test(expected = IllegalArgumentException.class)
  @When({ "!#coefficientsAreValid" })
  public void solveEquation3$thenThrowIllegalArgumentException() {
    ps.println(String.format("(a,b,c)=(%d,%d,%d)", a, b, c));
    new QuadraticEquationSolver(
        a,
        b,
        c).solve();
  }

  @Test
  @When({ "#*" })
  public void solveEquation$thenSolved() {
    ps.println(String.format("(a,b,c)=(%d,%d,%d)", a, b, c));
    QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
        c).solve();
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
  }
}
