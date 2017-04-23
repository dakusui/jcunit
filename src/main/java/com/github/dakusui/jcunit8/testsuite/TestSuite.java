package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * As a result of tuple suite generation, tuples that are identical if they are
 * converted back to parameter space can be created.
 * <p>
 * This class eliminates those tuples on its construction.
 */
public interface TestSuite<T> extends List<TestCase<T>> {

  class Builder<T> {
    private final ParameterSpace     parameterSpace;
    private LinkedHashSet<Tuple> regularTuples  = new LinkedHashSet<>();
    private LinkedHashSet<Tuple> negativeTuples = new LinkedHashSet<>();

    public Builder(ParameterSpace parameterSpace) {
      this.parameterSpace = requireNonNull(parameterSpace);
    }

    Builder<T> addToRegularTuples(Tuple in) {
      regularTuples.add(new Tuple.Builder().putAll(requireNonNull(in)).build());
      return this;
    }

    public Builder<T> addAllToRegularTuples(Collection<? extends Tuple> collection) {
      collection.forEach(Builder.this::addToRegularTuples);
      return this;
    }

    Builder<T> addToNegativeTuples(Tuple in) {
      negativeTuples.add(new Tuple.Builder().putAll(requireNonNull(in)).build());
      return this;
    }


    public Builder<T> addAllToNegativeTuples(Collection<? extends Tuple> collection) {
      collection.forEach(Builder.this::addToNegativeTuples);
      return this;
    }


    public TestSuite<T> build() {
      List<Tuple> tuples = new ArrayList<Tuple>() {{
        this.addAll(regularTuples);
        this.addAll(negativeTuples);
      }};
      class Impl<U> extends AbstractList<TestCase<U>> implements TestSuite<U> {
        private Impl() {
        }

        @Override
        public TestCase<U> get(int index) {
          U object = (U) tuples.get(index);
          if (index < regularTuples.size()) {
            return TestCase.Category.REGULAR.createTestCase(
                object,
                Collections.emptyList()
            );
          }
          return TestCase.Category.NEGATIVE.createTestCase(
              object,
              parameterSpace.getConstraints().stream()
                  .filter((Constraint constraint) -> !constraint.test(tuples.get(index)))
                  .collect(Collectors.toList()));
        }

        @Override
        public int size() {
          return tuples.size();
        }
      }
      return new Impl<>();
    }
  }
}
