package com.github.dakusui.jcunit8.testutils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.function.Function;
import java.util.function.Predicate;

public interface TestOracle<T, U> extends Predicate<T> {
  Function<T, U> transformer();

  Predicate<U> tester();

  static <T> TestOracle<T, T> create(Predicate<T> tester) {
    return new TestOracle<T, T>() {
      @Override
      public Function<T, T> transformer() {
        return self -> self;
      }

      @Override
      public Predicate<T> tester() {
        return tester;
      }

      @Override
      public boolean test(T t) {
        return tester.test(t);
      }
    };
  }

  static <T, U> Matcher<T> toMatcher(TestOracle<T, U> oracle) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(Object item) {
        //noinspection unchecked
        return oracle.test((T) item);
      }

      @Override
      public void describeTo(Description description) {

      }
    };
  }
}
