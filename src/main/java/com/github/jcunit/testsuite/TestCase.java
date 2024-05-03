package com.github.jcunit.testsuite;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;

import java.util.List;

public interface TestCase {
  enum Category {
    SEED,
    REGULAR,
    NEGATIVE;

    TestCase createTestCase(Tuple testData, List<Constraint> violatedConstraints) {
      return new TestCase() {
        @Override
        public Tuple getTestData() {
          return testData;
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
          return String.format("%s:%s:%s", this.getCategory(), this.getTestData(), violatedConstraints);
        }
      };
    }

  }

  Tuple getTestData();

  Category getCategory();

  List<Constraint> violatedConstraints();
}
