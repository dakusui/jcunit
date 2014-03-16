package com.github.dakusui.jcunit.ut.ipo;

import com.github.dakusui.jcunit.generators.ipo.TestSpace;
import com.github.dakusui.jcunit.generators.ipo.optimizers.GreedyIPOOptimizer;
import com.github.dakusui.jcunit.generators.ipo.optimizers.IPOOptimizer;

public class ExamplesWithGreedyOptimizer extends IPOExamples {

  @Override
  protected IPOOptimizer createOptimizer(TestSpace space) {
    return new GreedyIPOOptimizer(space);
  }

  @Override
  protected int expected01$3_3() {
    return 9;
  }

  @Override
  protected int expected02$3_13() {
    return 22;
  }

  @Override
  protected int expected03$4_15$3_17$2_20() {
    return 39;
  }

  @Override
  protected int expected04$4_1$3_30$2_35() {
    return 31;
  }

  @Override
  protected int expected05$2_100() {
    return 20;
  }

  @Override
  protected int expected06$10_20() {
    return 255;
  }

}
