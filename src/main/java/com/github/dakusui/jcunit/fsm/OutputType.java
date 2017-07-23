package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.utils.Checks;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.function.Predicate;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Types of an expectation. Each element represents an expectation of its
 * relevant method.
 */
public enum OutputType {
  /**
   * Expects that an exception is thrown of the method.
   */
  EXCEPTION_THROWN("thrown") {
    public String entityType() {
      return "exception";
    }

    @Override
    public Matcher<Object> returnedValueMatcher(Predicate<Object> test) {
      throw new AssertionError("An exception is expected to be thrown, but it returned normally");
    }

    @Override
    public Matcher<Throwable> thrownExceptionMatcher(Predicate<Throwable> test) {
      return new BaseMatcher<Throwable>() {
        @Override
        public void describeTo(Description description) {
          description.appendText(describeExpectation(test));
        }

        @Override
        public boolean matches(Object o) {
          assertThat(o, CoreMatchers.instanceOf(Throwable.class));
          return test.test((Throwable) o);
        }
      };
    }
  },
  /**
   * Expects that a value is returned of the method.
   * No exception is thrown, in other words.
   */
  VALUE_RETURNED("returned") {
    @Override
    public String toString() {
      return "returned";
    }

    public String entityType() {
      return "value";
    }

    @Override
    public Matcher<Object> returnedValueMatcher(Predicate<Object> test) {
      return new BaseMatcher<Object>() {
        @Override
        public void describeTo(Description description) {
          description.appendText(describeExpectation(test));
        }

        @Override
        public boolean matches(Object o) {
          return test.test(o);
        }
      };
    }

    @Override
    public Matcher<Throwable> thrownExceptionMatcher(Predicate<Throwable> test) {
      throw new AssertionError("A value is expected to be returned, but an exception was thrown");
    }
  };

  public final String name;

  OutputType(String name) {
    this.name = Checks.checknotnull(name);
  }

  abstract protected String entityType();

  public abstract Matcher<Object> returnedValueMatcher(Predicate<Object> test);

  public abstract Matcher<Throwable> thrownExceptionMatcher(Predicate<Throwable> test);


  public String describeExpectation(Object matcher) {
    return format(
        ////
        // EXPECTATION:
        // "returned" / "thrown"
        // "value"    / "exception"
        // {matcher.toString()} (e.g. "is 'Hello'")
        "%s %s %s",
        this.name,
        this.entityType(),
        matcher
    );
  }
}
