package com.github.dakusui.jcunit8.tests.components.fsm;

import com.github.dakusui.jcunit8.testutils.ResultUtils;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static com.github.dakusui.jcunit8.testutils.UTUtils.matcher;
import static com.github.dakusui.jcunit8.testutils.UTUtils.oracle;

public class TurnstileTest {
  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void givenNormalTurnstile$whenPerform$thenAllPass() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(TurnstileExamples.Normal.class),
        matcher(
            oracle("{x}.wasSuccessful()", Result::wasSuccessful, "==true", v -> v),
            oracle("{x}.getRunCount()", Result::getRunCount, "==2", v -> v == 2)
        )
    );
  }

  @Test
  public void givenBrokenTurnstile$whenPerform$thenAllFail() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(TurnstileExamples.Broken.class),
        matcher(
            oracle("{x}.wasSuccessful()", Result::wasSuccessful, "==false", v -> !v),
            oracle("{x}.getRunCount()", Result::getRunCount, "==2", v -> v == 2)
        )
    );
  }
}
