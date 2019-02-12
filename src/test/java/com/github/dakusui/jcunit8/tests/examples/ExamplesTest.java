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
import com.github.dakusui.jcunit8.testutils.JUnit4TestUtils;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import static com.github.dakusui.crest.Crest.*;

public class ExamplesTest {
  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void quadraticEquationSolver() {
    assertThat(
        JUnitCore.runClasses(QuadraticEquationExample.class),
        allOf(
            asBoolean("wasSuccessful").isTrue().$(),
            asInteger("getRunCount").gt(0).$()
        ));
  }

  @Test
  public void flyingSpaghettiMonster() {
    assertThat(
        JUnitCore.runClasses(FlyingSpaghettiMonsterExample.class),
        allOf(
            asBoolean("wasSuccessful").isTrue().$(),
            asInteger("getRunCount").gt(0).$()
        ));
  }

  @Test
  public void bankAccount() {
    assertThat(
        JUnitCore.runClasses(BankAccountExample.class),
        allOf(
            asBoolean("wasSuccessful").isTrue().$(),
            ////
            // This number is bigger than 184, which is a number of test cases generated WITH seeds.
            // And this means JCUnit is not smart enough to generate a test suite from a regex.
            // I'm leaving this as is for now.
            asInteger("getRunCount").equalTo(196).$()
        ));
  }

  @Test
  public void quadraticEquationSolverWithSeeds() {
    assertThat(
        JUnitCore.runClasses(QuadraticEquationExampleWithSeeds.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getFailureCount").equalTo(2).$(),
            asInteger("getIgnoreCount").equalTo(2).$()
        ));
  }

  @Test
  public void config() {
    assertThat(
        JUnitCore.runClasses(ConfigExample.class),
        allOf(
            asBoolean("wasSuccessful").isTrue().$(),
            asInteger("getRunCount").equalTo(14).$()
        )
    );
  }

  @Test
  public void bankAccountWithSeeds() {
    assertThat(
        JUnitCore.runClasses(BankAccountExampleWithSeeds.class),
        allOf(
            asBoolean("wasSuccessful").isTrue().$(),
            asInteger("getRunCount").equalTo(184).$()
        )
    );
  }

  @Test
  public void helper() {
    assertThat(
        JUnitCore.runClasses(ParameterHelperExample.class),
        allOf(
            asBoolean("wasSuccessful").isTrue().$(),
            asInteger("getRunCount").equalTo(107).$()
        )
    );
  }

  @Test
  public void beforeAfterTest() {
    assertThat(
        JUnitCore.runClasses(BeforeAfter.class),
        allOf(
            asBoolean("wasSuccessful").isTrue().$(),
            asInteger("getRunCount").equalTo(11).$()
        )
    );
  }

  @Test
  public void unusedParameterTest() {
    assertThat(
        JUnitCore.runClasses(UnusedParameter.class),
        allOf(
            asBoolean(JUnit4TestUtils.funcWasSuccessful()).isTrue().$(),
            asInteger(JUnit4TestUtils.funcGetRunCount()).eq(2).$()
        )
    );
  }

  @Test
  public void parameterizedConstraint() {
    assertThat(
        JUnitCore.runClasses(ParameterizedConstraintExample.class),
        allOf(
            asBoolean(JUnit4TestUtils.funcWasSuccessful()).isTrue().$(),
            asInteger(JUnit4TestUtils.funcGetRunCount()).eq(2).$(),
            asInteger(JUnit4TestUtils.funcGetIgnoreCount()).eq(8).$(),
            asInteger(JUnit4TestUtils.funcGetFailureCount()).eq(0).$()
        )
    );
  }

}
