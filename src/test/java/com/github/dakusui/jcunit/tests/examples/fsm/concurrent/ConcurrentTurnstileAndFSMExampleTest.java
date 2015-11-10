package com.github.dakusui.jcunit.tests.examples.fsm.concurrent;

import com.github.dakusui.jcunit.examples.fsm.concurrent.ConcurrentTurnstileAndFSMExample;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;

public class ConcurrentTurnstileAndFSMExampleTest extends Metatest {
  public ConcurrentTurnstileAndFSMExampleTest() {
    super(ConcurrentTurnstileAndFSMExample.class, 12, 0, 0);
  }

  @Test
  public void testConcurrentTurnstileAndFSMExample() {
    runTests();
  }
}
