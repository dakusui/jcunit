package com.github.dakusui.jcunit.ut.ipo;

import com.github.dakusui.jcunit.compat.generators.ipo.IPOTestSpace;
import com.github.dakusui.jcunit.compat.generators.ipo.optimizers.IPOOptimizer;
import com.github.dakusui.jcunit.compat.generators.ipo.optimizers.NaiveIPOOptimizer;

public class ExamplesWithNaiveOptimizer extends IPOExamples {
  @Override
  protected int expected01$3_3() {
    return 9;
  }

  @Override
  protected int expected02$3_13() {
    return 27;
  }

  @Override
  protected int expected03$4_15$3_17$2_20() {
    return 73;
  }

  @Override
  protected int expected04$4_1$3_30$2_35() {
    return 52;
  }

  @Override
  protected int expected05$2_100() {
    return 22;
  }

  @Override
  protected int expected06$10_20() {
    return 286;
  }

  protected IPOOptimizer createOptimizer(IPOTestSpace space) {
    return new NaiveIPOOptimizer(space);
  }
}
