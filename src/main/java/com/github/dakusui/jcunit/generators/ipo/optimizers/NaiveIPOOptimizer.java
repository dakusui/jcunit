package com.github.dakusui.jcunit.generators.ipo.optimizers;

import com.github.dakusui.jcunit.generators.ipo.TestRun;
import com.github.dakusui.jcunit.generators.ipo.TestRunSet;
import com.github.dakusui.jcunit.generators.ipo.TestSpace;

public class NaiveIPOOptimizer extends IPOOptimizer {

  public NaiveIPOOptimizer(TestSpace space) {
    super(space);
  }

  @Override
  protected Object bestValueFor(TestRunSet currentTestRunSet, TestRun testRun, int fieldId) {
    return this.hgCandidates.get(hgCandidates.size() - 1);
  }

  @Override
  public Object optimizeInVG(TestRunSet currentTestRunSet, TestRun testRun,
      int i) {
    return this.space.value(i, 1);
  }

  @Override
  public TestRunSet createTestRunSet(int width) {
    return new TestRunSet(width);
  }
}
