package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.constraints.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.generators.TestCaseGenerator;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.GreedyIPO2Optimizer;

import java.util.Iterator;
import java.util.List;

public class IPO2TestCaseGenerator extends BaseTestCaseGenerator {
  List<Tuple> tests;
  @Override protected long initializeTestCases(String[] params,
      Factors factors) {
    IPO2 ipo2 = new IPO2(factors, 2, new
        NullConstraintManager(),
        new GreedyIPO2Optimizer());
    ipo2.ipo();
    this.tests = ipo2.getResult();
    return this.tests.size();
  }

  @Override public int getIndex(String factorName, long testId) {
    Tuple testCase = this.tests.get((int) testId);
    Object l = testCase.get(factorName);
    return getFactor(factorName).levels.indexOf(l);
  }
}
