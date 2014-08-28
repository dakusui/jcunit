package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.Utils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsFieldFunctionsTest {
  static class TestClass {
    @FactorField
    private Object f = new Object();
  }

  @Test
  public void test() {
    TestClass testObj = new TestClass();
    assertEquals(null, Utils.getField(testObj, "f", FactorField.class));
  }
}
