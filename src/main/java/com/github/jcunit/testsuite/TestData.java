package com.github.jcunit.testsuite;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;

import java.util.List;

public interface TestData {
  enum Category {
    SEED,
    REGULAR,
    NEGATIVE;

    TestData createTestCase(Tuple testDataTuple, List<Constraint> violatedConstraints) {
      return new TestData() {
        @Override
        public Tuple getTestDataTuple() {
          return testDataTuple;
        }

        @Override
        public Category getCategory() {
          return Category.this;
        }

        @Override
        public List<Constraint> violatingConstraints() {
          return violatedConstraints;
        }

        @Override
        public String toString() {
          return String.format("%s:%s:%s", this.getCategory(), this.getTestDataTuple(), violatedConstraints);
        }
      };
    }

  }

  Tuple getTestDataTuple();

  Category getCategory();

  List<Constraint> violatingConstraints();
}
