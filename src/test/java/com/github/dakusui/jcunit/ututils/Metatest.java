package com.github.dakusui.jcunit.ututils;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static org.junit.Assert.assertEquals;

public abstract class Metatest {
  public final int expectedFailureCount;
  public final int expectedRunCount;
  public final int expectedIgnoreCount;

  public Metatest(
      int expectedRunCount,
      int expectedFailureCount,
      int expectedIgnoreCount) {
    this.expectedRunCount = expectedRunCount;
    this.expectedFailureCount = expectedFailureCount;
    this.expectedIgnoreCount = expectedIgnoreCount;
  }

  public void runTests() {
    Result result = JUnitCore.runClasses(this.getClass());
    assertEquals(expectedFailureCount, result.getFailureCount());
    assertEquals(expectedRunCount, result.getRunCount());
    assertEquals(expectedIgnoreCount, result.getIgnoreCount());
  }
}
