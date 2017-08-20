package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.Test;

public class JoinExperiment10_n extends JoinExperimentBase {
  @Test
  public void whenJoinWith10_5$thenLetsSee() {
    exerciseJoin(
        new FactorSpaceSpec("R").addFactor(10, 10).build(),
        5);
  }

  @Override
  protected int strength() {
    return 2;
  }

  @Override
  protected FactorSpaceSpec lhsFactorSpaceSpec() {
    return new FactorSpaceSpec("L").addFactor(10, 10);
  }
}
