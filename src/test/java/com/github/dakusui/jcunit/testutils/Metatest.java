package com.github.dakusui.jcunit.testutils;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static org.junit.Assert.assertEquals;

/**
 * A class to verify generatedTuples of a test class is not changed.
 */
public abstract class Metatest {
  public final int expectedFailureCount;
  public final int expectedRunCount;
  public final int expectedIgnoreCount;
  private final Class<?> testClass;

  public Metatest(Class<?> testClass,
      int expectedRunCount,
      int expectedFailureCount,
      int expectedIgnoreCount) {
    this.testClass = testClass;
    this.expectedRunCount = expectedRunCount;
    this.expectedFailureCount = expectedFailureCount;
    this.expectedIgnoreCount = expectedIgnoreCount;
  }

  public Metatest(
      int expectedRunCount,
      int expectedFailureCount,
      int expectedIgnoreCount) {
    ////
    // It is not possible to pass the returned value from 'this.getClass()' to this(...) as
    // its argument.
    this.testClass = this.getClass();
    this.expectedRunCount = expectedRunCount;
    this.expectedFailureCount = expectedFailureCount;
    this.expectedIgnoreCount = expectedIgnoreCount;
  }

  public Result runTests() {
    Result result = JUnitCore.runClasses(testClass);
    assertEquals(expectedRunCount, result.getRunCount());
    assertEquals(expectedFailureCount, result.getFailureCount());
    assertEquals(expectedIgnoreCount, result.getIgnoreCount());
    return result;
  }
}
