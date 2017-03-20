package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * A user level factor.
 *
 * @param <T> Type of values held by this class.
 */
public interface Parameter<T> extends Factor<T> {
  String getName();

  List<Argument<T>> getArguments();

  FactorSpace.Internal toInternalFactorSpace();

  T composeValueFrom(Tuple tuple);

  abstract class Base<T> implements Parameter<T> {
    private final String name;

    Base(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return this.name;
    }

    protected abstract List<Internal> decompose();

    protected abstract List<Constraint> generateConstraints();

  }

  interface Simple<T> extends Parameter<T> {
    abstract class Impl<T> extends Base<T> implements Simple<T> {
      Impl(String name) {
        super(name);
      }
    }

    static <U> Simple<U> create(String name, U... args) {
      Internal internalFactor = Utils.createInternalFactor(name, args);

      return new Impl<U>(name) {
        @Override
        public List<U> getLevels() {
          return asList(args);
        }

        @Override
        public U composeValueFrom(Tuple tuple) {
          //noinspection unchecked
          return (U) tuple.get(name);
        }

        @Override
        public List<Internal> decompose() {
          return singletonList(internalFactor);
        }

        @Override
        public List<Argument<U>> getArguments() {
          return null;
        }

        @Override
        public FactorSpace.Internal toInternalFactorSpace() {
          return null;
        }

        @Override
        public List<Constraint> generateConstraints() {
          return emptyList();
        }
      };
    }

  }

  interface Fsm extends Parameter {
  }

  interface Regex<T, D> extends Parameter<List<T>> {
  }
}
