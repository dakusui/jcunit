package com.github.dakusui.jcunitx.tests.validation.testresources;

import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.JUnit4_13Workaround;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This is an "example" class, intended to be executed by a "real" test class.
 */
@SuppressWarnings("NewClassNamingConvention")
@RunWith(JCUnit8.class)
public class NoParameter extends JUnit4_13Workaround {
  @Test
  public void testMethod() {
  }
}
