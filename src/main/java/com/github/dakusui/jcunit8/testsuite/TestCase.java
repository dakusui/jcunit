package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;

import java.util.List;

public interface TestCase {
  enum Category {
    SEED,
    REGULAR,
    NEGATIVE;

    TestCase createTestCase(Tuple test, TestScenario.Factory testScenarioFactory, List<Constraint> violatedConstraints) {
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
        public TestScenario scenario() {
          return testScenarioFactory.create();
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

  TestScenario scenario();
}
