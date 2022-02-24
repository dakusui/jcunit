package com.github.dakusui.jcunit8.testsuite;

import java.util.List;

public interface TestScenario {
  List<TestInputConsumer> preSuiteProcedures();

  List<TestInputConsumer> preTestInputProcedures();

  List<TestInputConsumer> preOracleProcedures();

  List<TestOracle> oracles();

  List<TestInputConsumer> postOracleProcedures();

  List<TestInputConsumer> postTestInputProcedures();

  List<TestInputConsumer> postSuiteProcedures();
}
