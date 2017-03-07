package com.github.dakusui.jcunit8.model.testsuite;

public interface TestSuite<T> {

  class Builder {
    public TestSuite build() {
      return new TestSuite() {
      };
    }
  }
}
