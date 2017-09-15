package com.github.dakusui.jcunit8.tests.examples;

import com.github.dakusui.jcunit8.examples.bankaccount.BankAccountExample;
import com.github.dakusui.jcunit8.examples.beforesandafters.BeforeAfter;
import com.github.dakusui.jcunit8.examples.beforesandafters.UnusedParameter;
import com.github.dakusui.jcunit8.examples.config.ConfigExample;
import com.github.dakusui.jcunit8.examples.flyingspaghettimonster.FlyingSpaghettiMonsterExample;
import com.github.dakusui.jcunit8.examples.parameterhelper.ParameterHelperExample;
import com.github.dakusui.jcunit8.examples.parameterizedconstraint.ParameterizedConstraintExample;
import com.github.dakusui.jcunit8.examples.quadraticequation.QuadraticEquationExample;
import com.github.dakusui.jcunit8.examples.seed.BankAccountExampleWithSeeds;
import com.github.dakusui.jcunit8.examples.seed.QuadraticEquationExampleWithSeeds;
import com.github.dakusui.jcunit8.testutils.ResultUtils;
import com.github.dakusui.jcunit8.testutils.TestOracle;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.util.function.Function;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.crest.core.Printable.function;
import static com.github.dakusui.jcunit8.testutils.UTUtils.matcher;

public class ExamplesTest {
  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

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
  public void config() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(ConfigExample.class),
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
                "==16", v -> v == 16
            )
        )
    );
  }

  @Test
  public void unusedParameterTest() {
    assertThat(
        JUnitCore.runClasses(UnusedParameter.class),
        allOf(
            asBoolean(funcWasSuccessful()).isTrue().$(),
            asInteger(funcGetRunCount()).eq(2).$()
        )
    );
  }

  @Test
  public void parameterizedConstraint() {
    assertThat(
        JUnitCore.runClasses(ParameterizedConstraintExample.class),
        allOf(
            asBoolean(funcWasSuccessful()).isTrue().$(),
            asInteger(funcGetRunCount()).eq(2).$(),
            asInteger(funcGetIgnoreCount()).eq(8).$(),
            asInteger(funcGetFailureCount()).eq(0).$()
        )
    );
  }

  private static Function<Result, Integer> funcGetFailureCount() {
    return function("getFailureCount", Result::getFailureCount);
  }

  private static Function<Result, Integer> funcGetIgnoreCount() {
    return function("getIgnoreCount", Result::getIgnoreCount);
  }

  private static Function<Result, Integer> funcGetRunCount() {
    return function("getRunCount", Result::getRunCount);
  }

  private static Function<Result, Boolean> funcWasSuccessful() {
    return function("wasSuccessful", Result::wasSuccessful);
  }
}
