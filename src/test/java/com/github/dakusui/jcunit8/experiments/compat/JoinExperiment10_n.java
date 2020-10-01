package com.github.dakusui.jcunit8.experiments.compat;

import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import org.junit.Test;

import static com.github.dakusui.peerj.utils.CoveringArrayGenerationUtils.generateWithIpoGplus;

public class JoinExperiment10_n extends JoinExperimentBase {
  @Test
  public void whenJoinWith10_5$thenLetsSee() {
    FactorSpaceSpec rhsSpec = new FactorSpaceSpec("R").addFactors(10, 10);
    final FactorSpace r = rhsSpec.build();
    exerciseJoin(5, rhsSpec, generateWithIpoGplus(r, strength()));
  }

  @Override
  protected int strength() {
    return 2;
  }

  @Override
  protected FactorSpaceSpec lhsFactorSpaceSpec() {
    return new FactorSpaceSpec("L").addFactors(10, 10);
  }
}
