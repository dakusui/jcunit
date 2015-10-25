package com.github.dakusui.jcunit.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.core.SystemProperties;
import org.junit.Test;

public class GeophileTest {
  @Test
  public void geophileReplayerTest() {
    System.setProperty(SystemProperties.KEY.BASEDIR.key(), "src/test/resources");
    new GeophileReplayerExample().runTests();
  }
}
