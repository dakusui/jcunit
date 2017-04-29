package com.github.dakusui.jcunit.irregex.expressions;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static org.junit.Assert.assertEquals;

public class RegexExampleTest {
  @Test
  public void verifyRegexExample() {
    Result result = JUnitCore.runClasses(RegexExample.class);
    assertEquals(RegexExample.failureCount, result.getFailureCount());
    assertEquals(RegexExample.runCount, result.getRunCount());
    assertEquals(RegexExample.ignoreCount, result.getIgnoreCount());
  }
}
