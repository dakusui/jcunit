package com.github.dakusui.jcunit.tests.examples.fsm.turnstile;

import com.github.dakusui.jcunit.examples.fsm.turnstile.TurnstileExample;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;

public class TurnstileExampleTest extends Metatest {
  public TurnstileExampleTest() {
    super(TurnstileExample.class, 4, 0, 0);
  }

  @Test public void testTurnstileExample() {
    runTests();
  }
}
