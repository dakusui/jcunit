package com.github.dakusui.jcunitx.testsuite;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.factorspace.Constraint;

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
