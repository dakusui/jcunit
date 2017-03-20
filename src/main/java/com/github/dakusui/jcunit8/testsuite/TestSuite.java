package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * As a result of tuple suite generation, tuples that are identical if they are
 * converted back to parameter space can be created.
 * <p>
 * This class eliminates those tuples on its construction.
 *
 */
public interface TestSuite<T> extends List<T> {

  class Builder<T> {
    private final ParameterSpace     parameterSpace;
    private final Function<Tuple, T> concretizer;
    private LinkedHashSet<Tuple> tuples = new LinkedHashSet<>();

    public Builder(ParameterSpace parameterSpace, Function<Tuple, T> concretizer) {
      this.parameterSpace = requireNonNull(parameterSpace);
      this.concretizer = requireNonNull(concretizer);
    }

    public Builder<T> add(Tuple in) {
      requireNonNull(in);
      Tuple.Builder b = new Tuple.Builder();
      parameterSpace.getParameterNames().forEach(
          (String s) -> b.put(s, parameterSpace.getParameter(s).composeValueFrom(in))
      );
      tuples.add(b.build());
      return this;
    }

    public TestSuite<T> build() {
      List<Tuple> tuples = new ArrayList<>(this.tuples);
      class Impl<U> extends AbstractList<U> implements TestSuite<U> {
        private final Function<Tuple, U> concretizer;

        private Impl(Function<Tuple, U> concretizer) {
          this.concretizer = concretizer;
        }

        @Override
        public U get(int index) {
          return concretizer.apply(tuples.get(index));
        }

        @Override
        public int size() {
          return tuples.size();
        }
      }
      return new Impl<>(this.concretizer);
    }
  }
}
