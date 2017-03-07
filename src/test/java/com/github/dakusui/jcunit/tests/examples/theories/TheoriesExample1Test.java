package com.github.dakusui.jcunit.tests.examples.theories;

import com.github.dakusui.jcunit.examples.theories.TheoriesExample1;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;

public class TheoriesExample1Test extends Metatest {
  public TheoriesExample1Test() {
    super(TheoriesExample1.class, 1, 0, 0);
  }

  @Test public void testTheoriesExample1() {
    runTests();
  }
}
