package com.github.dakusui.jcunit8.tests.components.fsm;

import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static org.junit.runner.JUnitCore.runClasses;

public class TurnstileTest {
  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void givenNormalTurnstile$whenPerform$thenAllPass() {
    assertThat(
        runClasses(TurnstileExamples.Normal.class),
        allOf(
            asBoolean("wasSuccessful").isTrue().$(),
            asInteger("getRunCount").equalTo(2).$()
        )
    );
  }

  @Test
  public void givenBrokenTurnstile$whenPerform$thenAllFail() {
    assertThat(
        runClasses(TurnstileExamples.Broken.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getRunCount").equalTo(2).$()
        )
    );
  }
}
