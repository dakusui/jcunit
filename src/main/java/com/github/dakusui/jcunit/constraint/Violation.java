package com.github.dakusui.jcunit.constraint;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;

public interface Violation {
  public static class Builder {
    private Object id;
    private Tuple  testCase;

    public Builder setId(Object name) {
      this.id = name;
      return this;
    }

    public Builder setTestCase(Tuple testCase) {
      this.testCase = testCase;
      return this;
    }
    public Violation build() {
      final Object name = Utils.checknotnull(this.id);
      final Tuple testCase = Utils.checknotnull(this.testCase);
      return new Violation() {
        @Override
        public Object getId() {
          return name;
        }
        @Override
        public Tuple getTestCase() {
          return testCase;
        }
      };
    }
  }
  public Object getId();
  public Tuple getTestCase();
}
