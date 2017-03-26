package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit8.factorspace.Constraint;

import java.util.List;

public interface TestCase<T> {
  enum Category {
    REGULAR,
    NEGATIVE;

    <T> TestCase<T> createTestCase(T test, List<Constraint> violatedConstraints) {
      return new TestCase<T>() {
        @Override
        public T get() {
          return test;
        }

        @Override
        public Category getCategory() {
          return Category.this;
        }

        @Override
        public List<Constraint> violatedConstraints() {
          return violatedConstraints;
        }
      };
    }

  }

  T get();

  Category getCategory();

  List<Constraint> violatedConstraints();
}
