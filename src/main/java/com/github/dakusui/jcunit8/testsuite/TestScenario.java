package com.github.dakusui.jcunit8.testsuite;

import java.util.List;

public interface TestScenario {
  List<RowConsumer> preSuiteProcedures();

  List<RowConsumer> preTestInputProcedures();

  List<RowConsumer> preOracleProcedures();

  List<TestOracle> oracles();

  List<RowConsumer> postOracleProcedures();

  List<RowConsumer> postTestInputProcedures();

  List<RowConsumer> postSuiteProcedures();
}
