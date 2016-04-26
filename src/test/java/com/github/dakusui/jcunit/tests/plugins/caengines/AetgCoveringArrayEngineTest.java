package com.github.dakusui.jcunit.tests.plugins.caengines;


import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.plugins.caengines.AetgCoveringArrayEngine;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class AetgCoveringArrayEngineTest {
  /**
   * Tests if AetgCoveringArrayGenerator creates a test suite.
   * Currently this test only checks the size.
   */
  @Test(timeout = 30000)
  public void aetgTestSuiteBuildingNegativeTestGenerationEnabled() {
    TestSuite testSuite = new TestSuite.Builder(new AetgCoveringArrayEngine(2, 0))
        .addFactor("factor1", 1, 2, 3)
        .addFactor("factor2", 1, 2, 3)
        .addFactor("factor3", 1, 2, 3)
        .disableNegativeTests()
        .build();
    for (TestCase each : testSuite) {
      System.out.println(each.getTuple());
    }
    assertEquals(9, testSuite.size());
  }

}
