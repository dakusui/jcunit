package com.github.dakusui.jcunit.tests.examples.nestedfield;

import com.github.dakusui.jcunit.examples.nestedfield.NestedFieldExample;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;

public class NestedFieldExampleTest extends Metatest {
  public NestedFieldExampleTest() {
    super(NestedFieldExample.class, 31, 0, 0);
  }

  @Test
  public void test() {
    runTests();
  }
}
