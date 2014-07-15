package com.github.dakusui.jcunit.extras.generators.ipo;

import com.github.dakusui.jcunit.compat.generators.ipo.IPOTestSpace;
import com.github.dakusui.jcunit.compat.generators.ipo.optimizers.IPOOptimizer;
import com.github.dakusui.jcunit.compat.generators.ipo.optimizers.LevelingIPOOptimizer;

public class ExamplesWithLevelingOptimizer extends IPOExamples {

  @Override
  protected IPOOptimizer createOptimizer(IPOTestSpace space) {
    return new LevelingIPOOptimizer(space);
  }

  @Override
  protected int expected01$3_3() {
    return 9;
  }

  @Override
  protected int expected02$3_13() {
    return 21;
  }

  @Override
  protected int expected03$4_15$3_17$2_20() {
    return 42;
  }

  @Override
  protected int expected04$4_1$3_30$2_35() {
    return 39;
  }

  @Override
  protected int expected05$2_100() {
    return 16;
  }

  @Override
  protected int expected06$10_20() {
    return 247;
  }

}
