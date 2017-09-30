package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.List;
import java.util.function.Consumer;

public interface TestScenario {
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

  class Base implements TestScenario {
    private final List<Runnable>        beforeAll;
    private final List<Consumer<Tuple>> beforeTestCase;
    private final List<Consumer<Tuple>> beforeTestOracle;
    private final List<Consumer<Tuple>> afterTestOracle;
    private final List<Consumer<Tuple>> afterTestCase;
    private final List<Runnable>        afterAll;
    private final List<TestOracle>      testOracles;

    public Base(
        List<Runnable> beforeAll,
        List<Consumer<Tuple>> beforeTestCase,
        List<Consumer<Tuple>> beforeTestOracle,
        List<Consumer<Tuple>> afterTestOracle,
        List<Consumer<Tuple>> afterTestCase,
        List<Runnable> afterAll,
        List<TestOracle> testOracles
    ) {
      this.beforeAll = beforeAll;
      this.beforeTestCase = beforeTestCase;
      this.beforeTestOracle = beforeTestOracle;
      this.afterTestOracle = afterTestOracle;
      this.afterTestCase = afterTestCase;
      this.afterAll = afterAll;
      this.testOracles = testOracles;
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
}
