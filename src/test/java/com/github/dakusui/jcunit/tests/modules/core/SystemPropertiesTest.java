package com.github.dakusui.jcunit.tests.modules.core;

import com.github.dakusui.jcunit.core.SystemProperties;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SystemPropertiesTest {
  @Test
  public void testRandomSeed() throws InterruptedException {
    System.clearProperty(SystemProperties.Key.RANDOMSEED.key());
    long before = System.currentTimeMillis();
    long seed1 = SystemProperties.randomSeed();
    long after = System.currentTimeMillis();
    assertTrue(seed1 >= before && seed1 <= after);
    Thread.sleep(10);
    assertEquals(seed1, SystemProperties.randomSeed());
  }
}
