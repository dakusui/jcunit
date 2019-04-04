package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.Test;

import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.generateWithIpoGplus;

public class JoinExperiment10_n extends JoinExperimentBase {
  @Test
  public void whenJoinWith10_5$thenLetsSee() {
    final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(10, 10).build();
    exerciseJoin(
        r,
        5, generateWithIpoGplus(
            r, strength()
        ));
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
