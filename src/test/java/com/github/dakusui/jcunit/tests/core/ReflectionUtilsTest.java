package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import org.junit.Test;

import java.lang.reflect.Method;

public class ReflectionUtilsTest {
  @SuppressWarnings("unused")
  private static Object hi() {
    return "hi";
  }

  @Test(expected = JCUnitException.class)
  public void testInvokeMethod1() throws Throwable {
    Method m = ReflectionUtilsTest.class.getDeclaredMethod("hi");
    ReflectionUtils.invoke(null, m);
  }

  @SuppressWarnings("unused")
  public static Object throwException() throws Exception {
    throw new Exception("Howdy!");
  }

  @Test(expected = JCUnitException.class)
  public void testInvokeMethod2() throws Throwable {
    Method m = ReflectionUtilsTest.class.getDeclaredMethod("throwException");
    ReflectionUtils.invoke(null, m);
  }
}
