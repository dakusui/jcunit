package com.github.dakusui.jcunit.tests.features.constraint;

import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

public class ConditionTest {
  @RunWith(JCUnit.class)
  public static class NegativeTest1 {
    @Condition(constraint = true)
    public boolean conditionMethod() {
      return false;
    }

    @Test
    public void testMethod() {
    }
  }

  @Test
  public void verifyTest1() {
    Result result = JUnitCore.runClasses(NegativeTest1.class);
    assertEquals(1, result.getFailureCount());
    assertEquals(1, result.getRunCount());
    assertEquals(0, result.getIgnoreCount());
  }
}