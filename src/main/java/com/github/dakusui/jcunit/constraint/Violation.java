package com.github.dakusui.jcunit.constraint;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.io.Serializable;

public interface Violation {
  public static class Builder {
    private Serializable id;
    private Tuple  testCase;

    public Builder setId(Serializable id) {
      this.id = id;
      return this;
    }

    public Builder setTestCase(Tuple testCase) {
      this.testCase = testCase;
      return this;
    }
    public Violation build() {
      final Serializable id = Utils.checknotnull(this.id);
      final Tuple testCase = Utils.checknotnull(this.testCase);
      return new Violation() {
        @Override
        public Serializable getId() {
          return id;
        }
        @Override
        public Tuple getTestCase() {
          return testCase;
        }
      };
    }
  }
  public Serializable getId();
  public Tuple getTestCase();
}
