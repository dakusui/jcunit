package com.github.dakusui.jcunit.extras.examples;

import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.framework.utils.TestUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import static org.junit.Assert.assertEquals;

public class TestArrayGeneratorsTest {
  @Test
  public void simpleTestArrayGenerator() throws Exception {
    Result result = JUnitCore.runClasses(CalcTest1.class);
    assertEquals(0, result.getFailureCount());
  }

  @Test
  public void cartesianTestArrayGenerator() throws Exception {
    Result result = JUnitCore.runClasses(CalcTest2.class);
    assertEquals(245, result.getRunCount());
    assertEquals(43 + 7, result.getFailureCount());
  }

  @Test
  public void pairwiseTestArrayGenenrator() throws Exception {
    Result result = JUnitCore.runClasses(CalcTest3.class);
    for (Failure f : result.getFailures()) {
      System.out.println(f);
    }
    assertEquals(49, result.getRunCount());
    assertEquals(3 + 6, result.getFailureCount());
  }

  @Test
  public void bestPairwiseTestArrayGenenrator() throws Exception {
    Result result = JUnitCore.runClasses(CalcTest3_2.class);
    for (Failure f : result.getFailures()) {
      System.out.println(f);
    }
    assertEquals(49, result.getRunCount());
    assertEquals(3 + 6, result.getFailureCount());
  }

  @Test
  public void automaticStoreLoadMechanismTest() throws Exception {
    System.setProperty(SystemProperties.KEY.BASEDIR.key(), TestUtils
        .createTempDirectory().getAbsolutePath());
    try {
      {
        // //
        // First run.
        Result result = JUnitCore.runClasses(CalcTest5.class);
        for (Failure f : result.getFailures()) {
          System.out.println(f);
        }
        assertEquals(1, result.getRunCount());
        assertEquals(1, result.getFailureCount());
      }
      {
        // //
        // Second run.
        Result result = JUnitCore.runClasses(CalcTest5.class);
        for (Failure f : result.getFailures()) {
          System.out.println(f);
        }
        assertEquals(1, result.getRunCount());
        assertEquals(0, result.getFailureCount());
      }
    } finally {
      System.clearProperty(SystemProperties.KEY.BASEDIR.key());
    }

  }

  @Test
  public void customTestArrayGenenrator() throws Exception {
    Result result = JUnitCore.runClasses(CalcTest6.class);
    for (Failure f : result.getFailures()) {
      System.out.println(f);
    }
    assertEquals(2, result.getRunCount());
    assertEquals(0, result.getFailureCount());
  }

}
