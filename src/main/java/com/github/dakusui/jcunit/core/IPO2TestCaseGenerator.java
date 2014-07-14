package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.GreedyIPO2Optimizer;

import java.util.List;

public class IPO2TestCaseGenerator extends BaseTestCaseGenerator {
  List<Tuple> tests;

  @Override public Tuple getTestCase(int testId) {
    return this.tests.get((int) testId);
  }

  @Override protected long initializeTestCases(Object[] params) {
    IPO2 ipo2 = new IPO2(
        this.getFactors(),
        2,
        this.getConstraintManager(),
        new GreedyIPO2Optimizer());
    ////
    // Wire constraint manager.
    this.getConstraintManager().addObserver(ipo2);
    ////
    // Perform IPO algorithm.
    ipo2.ipo();
    this.tests = ipo2.getResult();
    return this.tests.size();
  }
}
