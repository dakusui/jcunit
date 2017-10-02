package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public interface TestScenario {
  interface Factory {
    TestScenario create();
  }
  List<Runnable> preSuiteProcedures();

  List<Consumer<Tuple>> preTestCaseProcedures();

  List<Consumer<Tuple>> preOracleProcedures();

  /**
   * Returns test oracles used in this test suite.
   *
   * @return a list of test oracles.
   */
  List<TestOracle> getTestOracles();

  List<Consumer<Tuple>> postTestOracleProcedures();

  List<Consumer<Tuple>> postTestCaseProcedures();

  List<Runnable> postSuite();

  class Builder<B extends Builder<B>> {
    protected final List<Runnable>        preSuiteProcedures     = new LinkedList<>();
    protected final List<Consumer<Tuple>> preTestCaseProcedures  = new LinkedList<>();
    protected final List<Consumer<Tuple>> preOracleProcedures    = new LinkedList<>();
    protected final List<Consumer<Tuple>> postOracleProcedures   = new LinkedList<>();
    protected final List<Consumer<Tuple>> postTestCaseProcedures = new LinkedList<>();
    protected final List<Runnable>        postSuiteProcedures    = new LinkedList<>();
    protected final List<TestOracle>      testOracles            = new LinkedList<>();

    @SuppressWarnings("unchecked")
    public B addPreTestSuiteProcedure(Runnable procedure) {
      this.preSuiteProcedures.add(procedure);
      return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B addPreTestCaseProcedure(Consumer<Tuple> procedure) {
      this.preTestCaseProcedures.add(procedure);
      return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B addPreOracleProcedure(Consumer<Tuple> procedure) {
      this.preOracleProcedures.add(procedure);
      return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B addTestOracle(TestOracle oracle) {
      this.testOracles.add(oracle);
      return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B addPostOracleProcedure(Consumer<Tuple> procedure) {
      this.postOracleProcedures.add(procedure);
      return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B addPostTestCaseProcedure(Consumer<Tuple> procedure) {
      this.postTestCaseProcedures.add(procedure);
      return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B addPostTestSuiteProcedure(Runnable procedure) {
      this.postSuiteProcedures.add(procedure);
      return (B) this;
    }

    public TestScenario build() {
      return new Base(
          this.preSuiteProcedures,
          this.preTestCaseProcedures,
          this.preOracleProcedures,
          this.testOracles,
          this.postOracleProcedures,
          this.postTestCaseProcedures,
          this.postSuiteProcedures
      );
    }
  }

  class Base implements TestScenario {
    private final List<Runnable>        preSuite;
    private final List<Consumer<Tuple>> preTestCase;
    private final List<Consumer<Tuple>> preOracle;
    private final List<TestOracle>      testOracles;
    private final List<Consumer<Tuple>> postOracle;
    private final List<Consumer<Tuple>> postTestCase;
    private final List<Runnable>        postSuite;

    public Base(
        List<Runnable> preSuite,
        List<Consumer<Tuple>> preTestCase,
        List<Consumer<Tuple>> preOracle,
        List<TestOracle> testOracles,
        List<Consumer<Tuple>> postOracle,
        List<Consumer<Tuple>> postTestCase,
        List<Runnable> postSuite
    ) {
      this.preSuite = preSuite;
      this.preTestCase = preTestCase;
      this.preOracle = preOracle;
      this.postOracle = postOracle;
      this.postTestCase = postTestCase;
      this.postSuite = postSuite;
      this.testOracles = testOracles;
    }

    @Override
    public List<Runnable> preSuiteProcedures() {
      return preSuite;
    }

    @Override
    public List<Consumer<Tuple>> preTestCaseProcedures() {
      return preTestCase;
    }

    @Override
    public List<Consumer<Tuple>> preOracleProcedures() {
      return preOracle;
    }

    @Override
    public List<TestOracle> getTestOracles() {
      return testOracles;
    }

    @Override
    public List<Consumer<Tuple>> postTestOracleProcedures() {
      return postOracle;
    }

    @Override
    public List<Consumer<Tuple>> postTestCaseProcedures() {
      return postTestCase;
    }

    @Override
    public List<Runnable> postSuite() {
      return postSuite;
    }
  }
}
