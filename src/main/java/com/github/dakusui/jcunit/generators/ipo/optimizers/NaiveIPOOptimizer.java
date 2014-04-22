package com.github.dakusui.jcunit.generators.ipo.optimizers;

import com.github.dakusui.jcunit.generators.ipo.IPOTestRun;
import com.github.dakusui.jcunit.generators.ipo.IPOTestRunSet;
import com.github.dakusui.jcunit.generators.ipo.TestSpace;

public class NaiveIPOOptimizer extends IPOOptimizer {

  public NaiveIPOOptimizer(TestSpace space) {
    super(space);
  }

  @Override
  protected Object bestValueFor(IPOTestRunSet currentTestRunSet, IPOTestRun testRun, int fieldId) {
    return this.hgCandidates.get(hgCandidates.size() - 1);
  }

  @Override
  public Object optimizeInVG(IPOTestRunSet currentTestRunSet, IPOTestRun testRun,
      int i) {
    return this.space.value(i, 1);
  }

  @Override
  public IPOTestRunSet createTestRunSet(int width) {
    return new IPOTestRunSet(width);
  }
}
