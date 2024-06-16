package com.github.jcunit.testsuite;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.core.tuples.TupleUtils;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.ParameterSpace;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * As a result of tuple suite generation, tuples that are identical if they are
 * converted back to parameter space can be created.
 * <p>
 * This class eliminates those tuples on its construction.
 */
public interface TestSuite extends List<TestData> {
  /**
   * Returns a parameter space from which this instance is created.
   *
   * @return parameter space
   */
  ParameterSpace getParameterSpace();

  class Builder<T> {
    private final ParameterSpace parameterSpace;
    private final List<TestData> testCases = new LinkedList<>();

    public Builder(ParameterSpace parameterSpace) {
      this.parameterSpace = requireNonNull(parameterSpace);
    }

    public Builder<T> addAllToSeedTuples(Collection<? extends Tuple> collection) {
      collection.stream()
                .map(each -> toTestCase(TestData.Category.SEED, each))
                .forEach(testCases::add);
      return this;
    }

    public Builder<T> addAllToRegularTuples(Collection<? extends Tuple> collection) {
      collection.stream()
                .map(each -> toTestCase(TestData.Category.REGULAR, each))
                .forEach(testCases::add);
      return this;
    }

    public Builder<T> addAllToNegativeTuples(Collection<? extends Tuple> collection) {
      collection.stream()
                .map(each -> toTestCase(TestData.Category.NEGATIVE, each))
                .forEach(testCases::add);
      return this;
    }

    private TestData toTestCase(TestData.Category category, Tuple testDataTuple) {
      Tuple tuple = TupleUtils.copy(testDataTuple);
      return category.createTestCase(
          tuple,
          this.parameterSpace.getConstraints()
                             .stream()
                             .filter((Constraint constraint) -> !constraint.test(tuple))
                             .collect(Collectors.toList()));
    }

    public TestSuite build() {
      class Impl extends AbstractList<TestData> implements TestSuite {
        private final List<TestData> testCases;

        private Impl() {
          this.testCases = new ArrayList<TestData>(Builder.this.testCases.size()) {{
            Builder.this.testCases.stream()
                                  .filter(testCase -> this.stream()
                                                          .noneMatch(registered -> registered.getTestDataTuple().equals(testCase.getTestDataTuple())))
                                  .forEach(this::add);
          }};
        }

        @Override
        public TestData get(int index) {
          return this.testCases.get(index);
        }

        @Override
        public int size() {
          return this.testCases.size();
        }

        @Override
        public ParameterSpace getParameterSpace() {
          return parameterSpace;
        }

      }
      return new Impl();
    }
  }
}
