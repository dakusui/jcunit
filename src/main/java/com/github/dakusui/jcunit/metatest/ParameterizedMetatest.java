package com.github.dakusui.jcunit.metatest;

import org.junit.internal.requests.ClassRequest;
import org.junit.internal.requests.FilterRequest;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.manipulation.Filter;

public class ParameterizedMetatest extends Metatest {
  protected Request composeRequestForTestMethod(final Class<?> testClass, final String methodName) {
    return new FilterRequest(new ClassRequest(testClass), new Filter() {

      @Override
      public boolean shouldRun(Description description) {
        return false;
      }

      @Override
      public String describe() {
        return String.format("%s#%s", testClass.getCanonicalName(), methodName);
      }
    });
  }

  protected Expectation composeTestExpectation(Class<?> testClass, String testMethodName) {
    return null;
  }
}
