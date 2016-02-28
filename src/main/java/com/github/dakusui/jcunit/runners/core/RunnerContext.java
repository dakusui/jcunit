package com.github.dakusui.jcunit.runners.core;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;
import static com.github.dakusui.jcunit.core.reflect.ReflectionUtils.getFieldDeclaredIn;
import static com.github.dakusui.jcunit.core.reflect.ReflectionUtils.getFieldValueForcibly;

public interface RunnerContext {
  RunnerContext DUMMY = new Dummy();

  void setFactorSpace(FactorSpace factorSpace);

  void setConstraintChecker(ConstraintChecker constraintChecker);

  <T> T get(Key key);

  class Dummy implements RunnerContext {

    @Override
    public <T> T get(Key key) {
      throw new Error("TODO: FIXME: Use Base Instead");
    }

    @Override
    public void setFactorSpace(FactorSpace factorSpace) {
    }

    @Override
    public void setConstraintChecker(ConstraintChecker constraintChecker) {
    }
  }

  enum Key {
    DUMMY("") {
    },
    /**
     *
     */
    TEST_CLASS("testClass") {
    },
    FACTOR_SPACE("factorSpace") {
    },
    COVERINGARRAY_ENGINE("coveringArrayEngine") {
    },
    CONSTRAINT_CHECKER("constraintChecker") {
    };

    private final String name;

    Key(String name) {
      this.name = name;
    }

    public String getFieldName() {
      return this.name;
    }
  }

  /**
   * Fields whose name is equal to one of returned values of {@code getFieldName()} of {@code Key}s
   * are reflectively referenced through {@code get} method.
   *
   * <b>CAUTION:</b> This class is "mutable". Use with caution.
   */
  class Base implements RunnerContext {

    // See class level Javadoc.
    @SuppressWarnings("unused")
    private final Class<?> testClass;

    // See class level Javadoc.
    @SuppressWarnings("unused")
    private FactorSpace factorSpace;

    // See class level Javadoc.
    @SuppressWarnings("unused")
    private ConstraintChecker constraintChecker;
    // See class level Javadoc.
    @SuppressWarnings("unused")
    private CoveringArrayEngine coveringArrayEngine;

    public Base(Class<?> testClass) {
      this.testClass = Checks.checknotnull(testClass);

    }

    @Override
    public void setFactorSpace(FactorSpace factorSpace) {
      this.factorSpace = Checks.checknotnull(factorSpace);
    }

    @Override
    public void setConstraintChecker(ConstraintChecker constraintChecker) {
      this.constraintChecker = Checks.checknotnull(constraintChecker);
    }

    @Override
    public <T> T get(Key key) {
      return getFieldValueForcibly(this, getFieldDeclaredIn(this.getClass(), checknotnull(key).getFieldName()));
    }
  }
}
