package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;

import java.io.IOException;
import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Function;
import java.util.function.Predicate;

public interface TestOracle extends TupleConsumer {
  interface Result extends Formattable {
    enum Exit {
      RETURNING_VALUE {
        @Override
        public String toString() {
          return "returned";
        }
      },
      THROWING_EXCEPTION {
        @Override
        public String toString() {
          return "thrown";
        }
      }
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

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision) {
      try {
        formatter.out().append(String.format("%s was %s", value(), exitedWith()));
      } catch (IOException e) {
        throw Checks.wrap(e);
      }
    }
  }

  interface Assertion {
    default <V> void assertThat(V value, Predicate<V> check) {
      if (!check.test(value))
        throw new AssertionError(String.format("%s did not satisfy '%s'", value, check));
    }
  }

  Predicate<Tuple> shouldInvoke();

  Function<Tuple, Result> when();

  Predicate<Result> then();

  default String getName() {
    return String.format("%s(%s(s))==true", then(), when());
  }

  @Override
  default void accept(Tuple testInput) {
    assertion().assertThat(when().apply(testInput), then());
  }

  default Assertion assertion() {
    return new Assertion() {
    };
  }
}
