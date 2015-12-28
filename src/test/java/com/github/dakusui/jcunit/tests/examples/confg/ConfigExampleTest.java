package com.github.dakusui.jcunit.tests.examples.confg;

import com.github.dakusui.jcunit.examples.config.ConfigExample;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;

public class ConfigExampleTest extends Metatest {
  public ConfigExampleTest() {
    super(ConfigExample.class, 17, 0, 0);
  }

  @Test
  public void testConfigExample() {
    runTests();
  }
}
