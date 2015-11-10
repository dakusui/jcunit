package com.github.dakusui.jcunit.examples.fsm.concurrent;

import com.github.dakusui.jcunit.tests.examples.fsm.concurrent.ConcurrentTurnstileExample;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;

public class ConcurrentTurnstileExampleTest extends Metatest {
  public ConcurrentTurnstileExampleTest() {
    super(
        ConcurrentTurnstileExample.class,
        4,
        0,
        0
    );
  }

  @Test
  public void testConcurrentTurnstileExample() {
    runTests();
  }
}
