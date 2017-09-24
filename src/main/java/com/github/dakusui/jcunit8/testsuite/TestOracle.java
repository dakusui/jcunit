package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import java.util.function.Function;
import java.util.function.Predicate;

public interface TestOracle extends
    Function<Tuple, TestOracle.Result>,
    Predicate<TestOracle.Result> {
  interface Result {
    enum Exit {
      RETURNING_VALUE,
      THROWING_EXCEPTION
    }

    Exit exitedWith();

    <V> V value();

    static Result thrown(Throwable throwable) {
      return new Result() {
        @Override
        public Exit exitedWith() {
          return Exit.THROWING_EXCEPTION;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <V> V value() {
          return (V) throwable;
        }
      };
    }
    static Result returned(Object value) {
      return new Result() {
        @Override
        public Exit exitedWith() {
          return Exit.RETURNING_VALUE;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <V> V value() {
          return (V) value;
        }
      };
    }
  }

  boolean shouldInvoke(Tuple tuple);

  String getName();

}
