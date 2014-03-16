package com.github.dakusui.jcunit.generators.ipo.optimizers;

import com.github.dakusui.jcunit.generators.ipo.TestRun;
import com.github.dakusui.jcunit.generators.ipo.TestRunSet;
import com.github.dakusui.jcunit.generators.ipo.TestSpace;

public class ModuloIPOOptimizer extends IPOOptimizer {

  private int counter;

  public ModuloIPOOptimizer(TestSpace space) {
    super(space);
    this.counter = 0;
  }

  @Override
  protected Object bestValueFor(TestRunSet currentTestRunSet, TestRun testRun,
      int fieldId) {
    Object ret = this.hgCandidates.get(this.counter % this.hgCandidates.size());
    this.counter++;
    return ret;
  }

  @Override
  public Object optimizeInVG(TestRunSet currentTestRunSet, TestRun testRun,
      int i) {
    Object[] values = this.space.domainOf(i);
    Object ret = values[this.counter % values.length];
    return ret;
  }

  @Override
  public TestRunSet createTestRunSet(int width) {
    return new TestRunSet(width);
  }

}
