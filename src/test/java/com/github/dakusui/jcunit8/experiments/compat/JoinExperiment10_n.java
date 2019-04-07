package com.github.dakusui.jcunit8.experiments.compat;

import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.Test;

import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.generateWithIpoGplus;

public class JoinExperiment10_n extends JoinExperimentBase {
  @Test
  public void whenJoinWith10_5$thenLetsSee() {
    FactorSpaceSpec rhsSpec = new FactorSpaceSpec("R").addFactor(10, 10);
    final FactorSpace r = rhsSpec.build();
    exerciseJoin(5, rhsSpec, generateWithIpoGplus(r, strength()));
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
