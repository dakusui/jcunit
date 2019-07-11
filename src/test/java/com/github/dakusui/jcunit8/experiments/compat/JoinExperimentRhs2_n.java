package com.github.dakusui.jcunit8.experiments.compat;

import com.github.dakusui.jcunit8.extras.normalizer.compat.FactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import org.junit.Test;

import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.generateWithIpoGplus;

public abstract class JoinExperimentRhs2_n extends JoinExperimentBase {

  @Test
  public void whenJoinWith2_10$thenLetsSee() {
    FactorSpaceSpecForExperiments rSpec = factorSpeceSpec("R", 10);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        )
    );
  }

  @Test
  public void whenJoinWith2_20$thenLetsSee() {
    FactorSpaceSpecForExperiments rSpec = factorSpeceSpec("R", 20);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        )
    );
  }

  @Test
  public void whenJoinWith2_30$thenLetsSee() {
    FactorSpaceSpecForExperiments rSpec = factorSpeceSpec("R", 30);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_40$thenLetsSee() {
    FactorSpaceSpecForExperiments rSpec = factorSpeceSpec("R", 40);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_50$thenLetsSee() {
    FactorSpaceSpecForExperiments rSpec = factorSpeceSpec("R", 50);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_60$thenLetsSee() {
    FactorSpaceSpecForExperiments rSpec = factorSpeceSpec("R", 60);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_70$thenLetsSee() {
    FactorSpaceSpecForExperiments rSpec = factorSpeceSpec("R", 70);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_80$thenLetsSee() {
    FactorSpaceSpecForExperiments rSpec = factorSpeceSpec("R", 80);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }


  @Test
  public void whenJoinWith2_90$thenLetsSee() {
    FactorSpaceSpecForExperiments rSpec = factorSpeceSpec("R", 90);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }
}
