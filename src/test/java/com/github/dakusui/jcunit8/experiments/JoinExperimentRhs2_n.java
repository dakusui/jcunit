package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.Test;

import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.generateWithIpoGplus;

public abstract class JoinExperimentRhs2_n extends JoinExperimentBase {

  @Test
  public void whenJoinWith2_10$thenLetsSee() {
    FactorSpaceSpec rSpec = factorSpeceSpec("R", 10);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        )
    );
  }

  @Test
  public void whenJoinWith2_20$thenLetsSee() {
    FactorSpaceSpec rSpec = factorSpeceSpec("R", 20);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        )
    );
  }

  @Test
  public void whenJoinWith2_30$thenLetsSee() {
    FactorSpaceSpec rSpec = factorSpeceSpec("R", 30);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_40$thenLetsSee() {
    FactorSpaceSpec rSpec = factorSpeceSpec("R", 40);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_50$thenLetsSee() {
    FactorSpaceSpec rSpec = factorSpeceSpec("R", 50);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_60$thenLetsSee() {
    FactorSpaceSpec rSpec = factorSpeceSpec("R", 60);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_70$thenLetsSee() {
    FactorSpaceSpec rSpec = factorSpeceSpec("R", 70);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_80$thenLetsSee() {
    FactorSpaceSpec rSpec = factorSpeceSpec("R", 80);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }


  @Test
  public void whenJoinWith2_90$thenLetsSee() {
    FactorSpaceSpec rSpec = factorSpeceSpec("R", 90);
    final FactorSpace r = rSpec.build();
    exerciseJoin(
        10, rSpec, generateWithIpoGplus(
            r, strength()
        ));
  }
}
