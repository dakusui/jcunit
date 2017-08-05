package com.github.dakusui.jcunit8.tests.examples;

import com.github.dakusui.jcunit8.examples.bankaccount.BankAccountExample;
import com.github.dakusui.jcunit8.examples.beforesandafters.BeforeAfter;
import com.github.dakusui.jcunit8.examples.beforesandafters.UnusedParameter;
import com.github.dakusui.jcunit8.examples.flyingspaghettimonster.FlyingSpaghettiMonsterExample;
import com.github.dakusui.jcunit8.examples.parameterhelper.ParameterHelperExample;
import com.github.dakusui.jcunit8.examples.quadraticequation.QuadraticEquationExample;
import com.github.dakusui.jcunit8.examples.seed.BankAccountExampleWithSeeds;
import com.github.dakusui.jcunit8.examples.seed.QuadraticEquationExampleWithSeeds;
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
            UTUtils.oracle("successful", Result::wasSuccessful)
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

  @Test
  public void quadraticEquationSolverWithSeeds() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(QuadraticEquationExampleWithSeeds.class),
        matcher(
            UTUtils.oracle("failed", result -> !result.wasSuccessful()),
            UTUtils.oracle(
                "{x}.getFailureCount", Result::getFailureCount,
                "==2", v -> v == 2
            ),
            UTUtils.oracle(
                "{x}.getIgnoreCount", Result::getIgnoreCount,
                "==1", v -> v == 1
            )
        )
    );
  }

  @Test
  public void bankAccountWithSeeds() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(BankAccountExampleWithSeeds.class),
        matcher(
            new TestOracle.Builder<Result, Result>()
                .withTransformer("f({x})->{x}", v -> v)
                .withTester("{x}.wasSuccessful()", Result::wasSuccessful)
                .build()
        )
    );
  }


  @Test
  public void helper() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(ParameterHelperExample.class),
        matcher(
            new TestOracle.Builder<Result, Result>()
                .withTransformer("f({x})->{x}", v -> v)
                .withTester("{x}.wasSuccessful()", Result::wasSuccessful)
                .build()
        )
    );
  }

  @Test
  public void beforeAfterTest() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(BeforeAfter.class),
        matcher(
            UTUtils.oracle("success", Result::wasSuccessful),
            UTUtils.oracle(
                "{x}.getRunCount", Result::getRunCount,
                "==14", v -> v == 14
            )
        )
    );
  }

  @Test
  public void unusedParameterTest() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(UnusedParameter.class),
        matcher(
            UTUtils.oracle("success", Result::wasSuccessful),
            UTUtils.oracle(
                "{x}.getRunCount", Result::getRunCount,
                "==2", v -> v == 2
            )
        )
    );
  }
}
