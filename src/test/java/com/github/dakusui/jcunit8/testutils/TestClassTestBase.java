package com.github.dakusui.jcunit8.testutils;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public abstract class TestClassTestBase {

  protected void verifyTestClass(Class<?> javaTestClass) {
    List<String> expectation = new LinkedList<>();
    List<String> actualResult = new LinkedList<>();

    JUnitCore jUnitCore = new JUnitCore();
    TestClass testClass = new TestClass(javaTestClass);
    for (FrameworkMethod m : testClass.getAnnotatedMethods(Test.class).stream().sorted(Comparator.comparing(FrameworkMethod::getName)).collect(Collectors.toList())) {
      Request request = Request.method(testClass.getJavaClass(), m.getName());
      Result result = jUnitCore.run(request);
      expectation.add(String.format("%s: %d: %s", formatResult(shouldPass(m.getName())), 1, m.getName()));
      actualResult.add(String.format("%s: %d: %s", formatResult(result.wasSuccessful()), result.getRunCount(), m.getName()));
    }

    assertEquals(
        String.join("\n", expectation),
        String.join("\n", actualResult)
    );
  }

  private static String formatResult(boolean wasSuccessful) {
    return wasSuccessful ? "PASS" : "FAIL";
  }

  private static boolean shouldPass(String methodName) {
    if (methodName.endsWith("thenPass"))
      return true;
    if (methodName.endsWith("thenFail"))
      return false;
    throw new IllegalArgumentException(methodName);
  }
}
