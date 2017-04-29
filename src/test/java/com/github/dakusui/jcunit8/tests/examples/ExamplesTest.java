package com.github.dakusui.jcunit8.tests.examples;

import com.github.dakusui.jcunit8.examples.bankaccount.BankAccountExample;
import com.github.dakusui.jcunit8.examples.flyingspaghettimonster.FlyingSpaghettiMonsterExample;
import com.github.dakusui.jcunit8.examples.quadraticequation.QuadraticEquationExample;
import com.github.dakusui.jcunit8.testutils.ResultUtils;
import com.github.dakusui.jcunit8.testutils.TestOracle;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static com.github.dakusui.jcunit8.testutils.UTUtils.matcher;

public class ExamplesTest {
  @Test
  public void quadraticEquationSolver() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(QuadraticEquationExample.class),
        matcher(
            UTUtils.oracle("failed", Result::wasSuccessful)
        )
    );
  }

  @Test
  public void flyingSpaghettiMonster() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(FlyingSpaghettiMonsterExample.class),
        matcher(
            UTUtils.oracle("successful", Result::wasSuccessful)
        )
    );
  }

  @Test
  public void bankAccount() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(BankAccountExample.class),
        matcher(
            new TestOracle.Builder<Result, Result>()
                .withTransformer("f({x})->{x}", v -> v)
                .withTester("{x}.wasSuccessful()", Result::wasSuccessful)
                .build()
        )
    );
  }
}
