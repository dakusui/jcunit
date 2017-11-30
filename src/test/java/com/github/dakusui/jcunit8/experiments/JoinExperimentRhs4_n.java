package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.Test;

public class JoinExperimentRhs4_n extends JoinExperimentBase {
  @Test
  public void whenJoinWith4_8$thenLetsSee() {
    exerciseJoin(
        new FactorSpaceSpec("R").addFactors(4, 10).build(),
        10);
  }

  @Override
  protected int strength() {
    return 2;
  }

  @Override
  protected FactorSpaceSpec lhsFactorSpaceSpec() {
    return new FactorSpaceSpec("L").addFactors(4, 2);
  }
}
