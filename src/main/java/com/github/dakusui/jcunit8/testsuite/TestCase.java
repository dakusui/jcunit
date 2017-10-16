package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;

import java.util.List;

public interface TestCase {
  enum Category {
    SEED,
    REGULAR,
    NEGATIVE;

    TestCase createTestCase(Tuple test, List<Constraint> violatedConstraints) {
      return new TestCase() {
        @Override
        public Tuple get() {
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

        @Override
        public String toString() {
          return String.format("%s:%s:%s", this.getCategory(), this.get(), violatedConstraints);
        }
      };
    }

  }

  Tuple get();

  Category getCategory();

  List<Constraint> violatedConstraints();
}
