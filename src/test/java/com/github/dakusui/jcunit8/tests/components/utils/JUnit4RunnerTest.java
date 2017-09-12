package com.github.dakusui.jcunit8.tests.components.utils;

import com.github.dakusui.crest.Crest;
import com.github.dakusui.jcunit8.examples.quadraticequation.QuadraticEquationExample;
import com.github.dakusui.jcunit8.runners.junit4.JUnit4Runner;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Result;

import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.assertThat;

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
            Crest.asBoolean(Result::wasSuccessful).isTrue().$(),
            Crest.asInteger(Result::getRunCount).eq(1).$()
        )
    );
  }

  @Test
  public void whenRunSpecificMethodForAnyTestCase$thenOnlyTheMethodIsExecutedForAllTestCases() {
    assertThat(
        new JUnit4Runner.Builder(QuadraticEquationExample.class).allTestCases().methodName("performScenario2").build().run(),
        allOf(
            Crest.asBoolean(Result::wasSuccessful).isTrue().$(),
            Crest.asInteger(Result::getRunCount).eq(46).$()
        )
    );
  }

  @Test
  public void whenRunAllMethodsForSpecificTestCase$thenAllMethodsAreExecutedForTheTestCase() {
    assertThat(
        new JUnit4Runner.Builder(QuadraticEquationExample.class).testCase(1).allMethods().build().run(),
        allOf(
            Crest.asBoolean(Result::wasSuccessful).isTrue().$(),
            Crest.asInteger(Result::getRunCount).eq(2).$()
        )
    );
  }

  @Test
  public void whenRunAllMethodsForAnyTestCase$thenAllMethodsAreExecutedForAllTestCases() {
    assertThat(
        new JUnit4Runner.Builder(QuadraticEquationExample.class).allTestCases().allTestCases().build().run(),
        allOf(
            Crest.asBoolean(Result::wasSuccessful).isTrue().$(),
            Crest.asInteger(Result::getRunCount).eq(92).$()
        )
    );
  }
}
