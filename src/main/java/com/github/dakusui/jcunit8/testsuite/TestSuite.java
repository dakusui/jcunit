package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * As a result of tuple suite generation, tuples that are identical if they are
 * converted back to parameter space can be created.
 * <p>
 * This class eliminates those tuples on its construction.
 */
public interface TestSuite extends List<TestCase> {
  /**
   * Returns a parameter space from which this instance is created.
   *
   * @return parameter space
   */
  ParameterSpace getParameterSpace();

  List<Runnable> beforeAll();

  List<Consumer<Tuple>> beforeTestCase();

  List<Consumer<Tuple>> beforeTestOracle();

  /**
   * Returns test oracles used in this test suite.
   *
   * @return a list of test oracles.
   */
  List<TestOracle> getTestOracles();

  List<Consumer<Tuple>> afterTestOracle();

  List<Consumer<Tuple>> afterTestCase();

  List<Runnable> afterAll();

  class Builder<T> {
    private final ParameterSpace parameterSpace;
    private final List<TestCase> testCases = new LinkedList<>();
    private final List<TestOracle> testOracles;
    private List<Runnable>        beforeAll        = new LinkedList<>();
    private List<Consumer<Tuple>> beforeTestCase   = new LinkedList<>();
    private List<Consumer<Tuple>> afterTestOracle  = new LinkedList<>();
    private List<Consumer<Tuple>> beforeTestOracle = new LinkedList<>();
    private List<Consumer<Tuple>> afterTestCase    = new LinkedList<>();
    private List<Runnable>        afterAll         = new LinkedList<>();

    public Builder(ParameterSpace parameterSpace, List<TestOracle> testOracles) {
      this.parameterSpace = requireNonNull(parameterSpace);
      this.testOracles = testOracles;
    }

    public Builder<T> addAllToSeedTuples(Collection<? extends Tuple> collection) {
      collection.stream().map(each -> toTestCase(TestCase.Category.SEED, each)).forEach(testCases::add);
      return this;
    }

    public Builder<T> addAllToRegularTuples(Collection<? extends Tuple> collection) {
      collection.stream().map(each -> toTestCase(TestCase.Category.REGULAR, each)).forEach(testCases::add);
      return this;
    }

    public Builder<T> addAllToNegativeTuples(Collection<? extends Tuple> collection) {
      collection.stream().map(each -> toTestCase(TestCase.Category.NEGATIVE, each)).forEach(testCases::add);
      return this;
    }

    private TestCase toTestCase(TestCase.Category category, Tuple testCaseTuple) {
      Tuple tuple = TupleUtils.copy(testCaseTuple);
      return category.createTestCase(
          tuple,
          testOracles,
          this.parameterSpace.getConstraints().stream()
              .filter((Constraint constraint) -> !constraint.test(tuple))
              .collect(Collectors.toList()));
    }

    public TestSuite build() {
      class Impl extends AbstractList<TestCase> implements TestSuite {
        private final List<TestCase>        testCases;
        private final List<Runnable>        beforeAll;
        private final List<Consumer<Tuple>> beforeTestCase;
        private final List<Consumer<Tuple>> beforeTestOracle;
        private final List<Consumer<Tuple>> afterTestOracle;
        private final List<Consumer<Tuple>> afterTestCase;
        private final List<Runnable>        afterAll;

        private Impl(
            List<Runnable> beforeAll,
            List<Consumer<Tuple>> beforeTestCase,
            List<Consumer<Tuple>> beforeTestOracle,
            List<Consumer<Tuple>> afterTestOracle,
            List<Consumer<Tuple>> afterTestCase,
            List<Runnable> afterAll
        ) {
          this.beforeAll = beforeAll;
          this.beforeTestCase = beforeTestCase;
          this.beforeTestOracle = beforeTestOracle;
          this.afterTestOracle = afterTestOracle;
          this.afterTestCase = afterTestCase;
          this.afterAll = afterAll;
          this.testCases = new ArrayList<TestCase>(Builder.this.testCases.size()) {{
            Builder.this.testCases.stream(
            ).filter(
                testCase -> stream().noneMatch(
                    registered -> registered.get().equals(testCase.get())
                )
            ).forEach(
                this::add
            );
          }};
        }

        @Override
        public TestCase get(int index) {
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

        @Override
        public List<Runnable> beforeAll() {
          return beforeAll;
        }

        @Override
        public List<Consumer<Tuple>> beforeTestCase() {
          return beforeTestCase;
        }

        @Override
        public List<Consumer<Tuple>> beforeTestOracle() {
          return beforeTestOracle;
        }

        @Override
        public List<TestOracle> getTestOracles() {
          return testOracles;
        }

        @Override
        public List<Consumer<Tuple>> afterTestOracle() {
          return afterTestOracle;
        }

        @Override
        public List<Consumer<Tuple>> afterTestCase() {
          return afterTestCase;
        }

        @Override
        public List<Runnable> afterAll() {
          return afterAll;
        }
      }
      return new Impl(
          beforeAll,
          beforeTestCase,
          beforeTestOracle,
          afterTestOracle,
          afterTestCase,
          afterAll
      );
    }
  }
}
