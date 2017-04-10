package com.github.dakusui.jcunit.tests.examples.theories;

import com.github.dakusui.jcunit.examples.theories.TheoriesExample2;
import com.github.dakusui.jcunit.testutils.Metatest;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * TheoriesWithJCUnit feature will be deprecated very soon. (Apr/10/2017).
 */
@Ignore
public class TheoriesExample2Test extends Metatest {
  public TheoriesExample2Test() {
    super(TheoriesExample2.class, 2, 0, 0);
  }

  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }
  @Test
  public void runTheoriesExample2() {
    runTests();
  }
}
