package com.github.dakusui.jcunit.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.core.utils.SystemProperties;
import org.junit.Test;

public class GeophileTest {
  @Test
  public void geophileReplayerTest() {
    System.setProperty(SystemProperties.Key.BASEDIR.key(), "src/test/resources");
    new GeophileReplayerExample().runTests();
  }
}
