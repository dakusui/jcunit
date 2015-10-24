package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.When;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

public class PreconditionTest {
  @RunWith(JCUnit.class)
  public static class TestClass {
    @SuppressWarnings("unused")
    @FactorField
    public boolean f1;
    @SuppressWarnings("unused")
    @FactorField
    public boolean f2;

    @SuppressWarnings("unused")
    public boolean precondition() {
      return f1;
    }

    @Test
    @When("precondition")
    public void test1() {
    }
  }

  @Test
  public void test() {
    Result result = JUnitCore.runClasses(TestClass.class);
    assertEquals(true, result.wasSuccessful());
    assertEquals(2, result.getIgnoreCount());
    assertEquals(2, result.getRunCount());
  }
}
