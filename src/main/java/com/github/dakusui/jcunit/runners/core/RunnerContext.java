package com.github.dakusui.jcunit.runners.core;

import com.github.dakusui.jcunit.core.Checks;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;
import static com.github.dakusui.jcunit.core.reflect.ReflectionUtils.getFieldDeclaredIn;
import static com.github.dakusui.jcunit.core.reflect.ReflectionUtils.getFieldValueForcibly;

public interface RunnerContext {
  enum Key {
    DUMMY("") {
    },
    TEST_CLASS("testClass") {
    };

    private final String name;

    Key(String name) {
      this.name = name;
    }

    public String getFieldName() {
      return this.name;
    }
  }

  Object get(Key key);

  class Dummy implements RunnerContext {

    @Override
    public Object get(Key key) {
      throw new Error("TODO: FIXME: Use Base Instead");
    }
  }

  /**
   * Fields whose name is equal to one of returned values of {@code getFieldName()} of {@code Key}s
   * are reflectively referenced through {@code get} method.
   */
  class Base implements RunnerContext {

    // See class level Javadoc.
    @SuppressWarnings("unused")
    private final Object testClass;

    public Base(Class<?> testClass) {
      this.testClass = Checks.checknotnull(testClass);

    }

    @Override
    public Object get(Key key) {
      return getFieldValueForcibly(this, getFieldDeclaredIn(this.getClass(), checknotnull(key).getFieldName()));
    }
  }
}
