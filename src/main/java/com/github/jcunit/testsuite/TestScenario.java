package com.github.jcunit.testsuite;

import java.util.List;

public interface TestScenario {
  List<TupleConsumer> preSuiteProcedures();

  List<TupleConsumer> preTestInputProcedures();

  List<TupleConsumer> preOracleProcedures();

  List<TestOracle> oracles();

  List<TupleConsumer> postOracleProcedures();

  List<TupleConsumer> postTestInputProcedures();

  List<TupleConsumer> postSuiteProcedures();
}
