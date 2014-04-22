package com.github.dakusui.jcunit.generators.ipo.optimizers;

import com.github.dakusui.jcunit.generators.ipo.IPOTestRun;
import com.github.dakusui.jcunit.generators.ipo.IPOTestRunSet;
import com.github.dakusui.jcunit.generators.ipo.IPOTestSpace;

public class ModuloIPOOptimizer extends IPOOptimizer {

  private int counter;

  public ModuloIPOOptimizer(IPOTestSpace space) {
    super(space);
    this.counter = 0;
  }

  @Override
  protected Object bestValueFor(IPOTestRunSet currentTestRunSet, IPOTestRun testRun,
      int fieldId) {
    Object ret = this.hgCandidates.get(this.counter % this.hgCandidates.size());
    this.counter++;
    return ret;
  }

  @Override
  public Object optimizeInVG(IPOTestRunSet currentTestRunSet, IPOTestRun testRun,
      int i) {
    Object[] values = this.space.domainOf(i);
    Object ret = values[this.counter % values.length];
    return ret;
  }

  @Override
  public IPOTestRunSet createTestRunSet(int width) {
    return new IPOTestRunSet(width);
  }

}
