package com.github.dakusui.jcunit.tests.examples.fsm.doublefsm;

import com.github.dakusui.jcunit.examples.fsm.doublefsm.DoubleFSMExample;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;

public class DoubleFSMExampleTest extends Metatest {
  public DoubleFSMExampleTest() {
    super(DoubleFSMExample.class, 4, 0, 0);
  }

  @Test public void testDoubleFSMExampleTest() {
    runTests();
  }
}
