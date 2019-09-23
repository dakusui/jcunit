package com.github.dakusui.jcunit8.tests.components.utils;

import com.github.dakusui.jcunit8.examples.quadraticequation.QuadraticEquationExample;
import com.github.dakusui.jcunit8.runners.junit4.JUnit4Runner;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.jcunit8.testutils.JUnit4TestUtils.funcGetRunCount;
import static com.github.dakusui.jcunit8.testutils.JUnit4TestUtils.funcWasSuccessful;

public class JUnit4RunnerTest {
  @BeforeClass
  public static void beforeClass() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void whenRunSpecificMethodForSpecificTestCase$thenOnlyTheMethodIsExecutedForTheTestCase() {
    assertThat(
        new JUnit4Runner.Builder(QuadraticEquationExample.class).testCase(1).methodName("performScenario2").build().run(),
        allOf(
            asBoolean(funcWasSuccessful()).isTrue().$(),
            asInteger(funcGetRunCount()).eq(1).$()
        )
    );
  }

  @Test
  public void whenRunSpecificMethodForAnyTestCase$thenOnlyTheMethodIsExecutedForAllTestCases() {
    assertThat(
        new JUnit4Runner.Builder(QuadraticEquationExample.class).allTestCases().methodName("performScenario2").build().run(),
        allOf(
            asBoolean(funcWasSuccessful()).isTrue().$(),
            asInteger(funcGetRunCount()).eq(46).$()
        )
    );
  }

  @Test
  public void whenRunSpecificMethodForAnyTestCaseInRange$thenOnlyTheMethodIsExecutedForTheTestCasesInRange() {
    assertThat(
        new JUnit4Runner.Builder(QuadraticEquationExample.class).testCasesInRange(0, 10).methodName("performScenario2").build().run(),
        allOf(
            asBoolean(funcWasSuccessful()).isTrue().$(),
            asInteger(funcGetRunCount()).eq(10).$()
        )
    );
  }

  @Test
  public void whenRunSpecificMethodForAnyTestCaseFrom$thenOnlyTheMethodIsExecutedForTheTestCasesFrom() {
    assertThat(
        new JUnit4Runner.Builder(QuadraticEquationExample.class).testCasesFrom(10).methodName("performScenario2").build().run(),
        allOf(
            asBoolean(funcWasSuccessful()).isTrue().$(),
            asInteger(funcGetRunCount()).eq(36).$()
        )
    );
  }

  @Test
  public void whenRunSpecificMethodForAnyTestCaseUntil$thenOnlyTheMethodIsExecutedForTheTestCasesUntil() {
    assertThat(
        new JUnit4Runner.Builder(QuadraticEquationExample.class).testCasesUntil(10).methodName("performScenario2").build().run(),
        allOf(
            asBoolean(funcWasSuccessful()).isTrue().$(),
            asInteger(funcGetRunCount()).eq(10).$()
        )
    );
  }

  @Test
  public void whenRunAllMethodsForSpecificTestCase$thenAllMethodsAreExecutedForTheTestCase() {
    assertThat(
        new JUnit4Runner.Builder(QuadraticEquationExample.class).testCase(1).allMethods().build().run(),
        allOf(
            asBoolean(funcWasSuccessful()).isTrue().$(),
            asInteger(funcGetRunCount()).eq(2).$()
        )
    );
  }

  @Test
  public void whenRunAllMethodsForAnyTestCase$thenAllMethodsAreExecutedForAllTestCases() {
    assertThat(
        new JUnit4Runner.Builder(QuadraticEquationExample.class).allTestCases().build().run(),
        allOf(
            asBoolean(funcWasSuccessful()).isTrue().$(),
            asInteger(funcGetRunCount()).eq(92).$()
        )
    );
  }
}
