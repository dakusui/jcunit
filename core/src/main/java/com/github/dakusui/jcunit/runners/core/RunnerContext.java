package com.github.dakusui.jcunit.runners.core;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static com.github.dakusui.jcunit.core.reflect.ReflectionUtils.getFieldDeclaredIn;
import static com.github.dakusui.jcunit.core.reflect.ReflectionUtils.getFieldValueForcibly;

public interface RunnerContext {
  RunnerContext DUMMY = new Dummy();

  void setFactors(Factors factors);

  void setConstraintChecker(ConstraintChecker constraintChecker);

  <T> T get(Key key);

  Factors getFactors();

  class Dummy implements RunnerContext {
    private Dummy() {
    }

    @Override
    public <T> T get(Key key) {
      throw new UnsupportedOperationException("FIXME: Use Base Instead");
    }

    @Override
    public Factors getFactors() {
      throw new UnsupportedOperationException("FIXME: Use Base Instead");
    }

    @Override
    public void setFactors(Factors factors) {
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
    FACTORS("factors") {
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
    private Factors factors;

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
    public void setFactors(Factors factors) {
      this.factors = Checks.checknotnull(factors);
    }

    @Override
    public void setConstraintChecker(ConstraintChecker constraintChecker) {
      this.constraintChecker = Checks.checknotnull(constraintChecker);
    }

    @Override
    public <T> T get(Key key) {
      return getFieldValueForcibly(this, getFieldDeclaredIn(this.getClass(), checknotnull(key).getFieldName()));
    }

    @Override
    public Factors getFactors() {
      return this.factors;
    }
  }
}
