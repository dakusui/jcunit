package com.github.dakusui.jcunit.tests.examples.fsm.nested;

import com.github.dakusui.jcunit.examples.fsm.nested.NestedFSMExample;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;

public class NestedFSMExampleTest extends Metatest {
  public NestedFSMExampleTest() {
    super(NestedFSMExample.class, 66, 0, 0);
  }

  @Test public void testNestedFSMExampleTest() {
    runTests();
  }
}
