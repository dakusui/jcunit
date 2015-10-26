package com.github.dakusui.jcunit.examples.theories;

import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.Before;
import org.junit.Test;

public class TheoriesExampleTest {
  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }
  @Test
  public void runTheoriesExample() {
    UTUtils.runTests(TheoriesExample.class, 2, 0, 0);
  }
}
