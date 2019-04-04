package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.Test;

import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.generateWithIpoGplus;

public abstract class JoinExperimentRhs2_n extends JoinExperimentBase {

  @Test
  public void whenJoinWith2_10$thenLetsSee() {
    final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 10).build();
    exerciseJoin(
        r,
        10, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_20$thenLetsSee() {
    final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 20).build();
    exerciseJoin(
        r,
        10, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_30$thenLetsSee() {
    final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 30).build();
    exerciseJoin(
        r,
        10, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_40$thenLetsSee() {
    final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 40).build();
    exerciseJoin(
        r,
        10, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_50$thenLetsSee() {
    final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 50).build();
    exerciseJoin(
        r,
        10, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_60$thenLetsSee() {
    final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 60).build();
    exerciseJoin(
        r,
        10, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_70$thenLetsSee() {
    final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 70).build();
    exerciseJoin(
        r,
        10, generateWithIpoGplus(
            r, strength()
        ));
  }

  @Test
  public void whenJoinWith2_80$thenLetsSee() {
    final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 80).build();
    exerciseJoin(
        r,
        10, generateWithIpoGplus(
            r, strength()
        ));
  }


  @Test
  public void whenJoinWith2_90$thenLetsSee() {
    final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 90).build();
    exerciseJoin(
        r,
        10, generateWithIpoGplus(
            r, strength()
        ));
  }
}
