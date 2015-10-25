package com.github.dakusui.jcunit.runners.core;

public interface RunnerContext {
  RunnerContext NULL = new RunnerContext() {
    @Override
    public Object get(KEY key) {
      return null;
    }
  };

  enum KEY {
    DUMMY, TEST_OBJECT
  }

  Object get(KEY key);
}
