package com.github.dakusui.jcunit.tests.examples.theories;

import com.github.dakusui.jcunit.examples.theories.TheoriesExample2;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;

public class TheoriesExampleTest {
  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }
  @Test
  public void runTheoriesExample() {
    UTUtils.runTests(TheoriesExample2.class, 2, 0, 0);
  }
}
