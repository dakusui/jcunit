package com.github.dakusui.jcunit8.experiments.compat;

import com.github.dakusui.jcunit8.testutils.testsuitequality.CompatFactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.extras.normalizer.compat.FactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import org.junit.Test;

import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.generateWithIpoGplus;

public class JoinExperiment10_n extends JoinExperimentBase {
  @Test
  public void whenJoinWith10_5$thenLetsSee() {
    FactorSpaceSpecForExperiments rhsSpec = new CompatFactorSpaceSpecForExperiments("R").addFactors(10, 10);
    final FactorSpace r = rhsSpec.build();
    exerciseJoin(5, rhsSpec, generateWithIpoGplus(r, strength()));
  }

  @Override
  protected int strength() {
    return 2;
  }

  @Override
  protected FactorSpaceSpecForExperiments lhsFactorSpaceSpec() {
    return new CompatFactorSpaceSpecForExperiments("L").addFactors(10, 10);
  }
}
