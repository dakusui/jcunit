package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.AArray;
import com.github.dakusui.jcunit8.factorspace.Constraint;

import java.util.List;

public interface TestCase {
  enum Category {
    SEED,
    REGULAR,
    NEGATIVE;

    TestCase createTestCase(AArray testInput, List<Constraint> violatedConstraints) {
      return new TestCase() {
        @Override
        public AArray getTestInput() {
          return testInput;
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
          return String.format("%s:%s:%s", this.getCategory(), this.getTestInput(), violatedConstraints);
        }
      };
    }

  }

  AArray getTestInput();

  Category getCategory();

  List<Constraint> violatedConstraints();
}
