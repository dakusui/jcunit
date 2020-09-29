package com.github.dakusui.jcunit8.experiments.generation;

import com.github.dakusui.jcunit8.testutils.testsuitequality.GenerationTestBase;
import com.github.dakusui.peerj.model.FactorSpaceSpecForExperiments;
import org.junit.Test;

public class GenerationExperiment extends GenerationTestBase {
  @Test
  public void generate2_10() {
    exerciseGeneration(new FactorSpaceSpecForExperiments("F").addFactors(2, 10), 2, 10);
  }

  @Test
  public void generate2_20() {
    exerciseGeneration(new FactorSpaceSpecForExperiments("F").addFactors(2, 20), 2, 10);
  }

  @Test
  public void generate2_30() {
    exerciseGeneration(new FactorSpaceSpecForExperiments("F").addFactors(2, 30), 2, 10);
  }

  @Test
  public void generate2_40() {
    exerciseGeneration(new FactorSpaceSpecForExperiments("F").addFactors(2, 40), 2, 10);
  }

  @Test
  public void generate2_50() {
    exerciseGeneration(new FactorSpaceSpecForExperiments("F").addFactors(2, 50), 2, 10);
  }

  @Test
  public void generate2_60() {
    exerciseGeneration(new FactorSpaceSpecForExperiments("F").addFactors(2, 60), 2, 5);
  }

  @Test
  public void generate2_70() {
    exerciseGeneration(new FactorSpaceSpecForExperiments("F").addFactors(2, 70), 2, 5);
  }

  @Test
  public void generate2_80() {
    exerciseGeneration(new FactorSpaceSpecForExperiments("F").addFactors(2, 80), 2, 5);
  }

  @Test
  public void generate2_90() {
    exerciseGeneration(new FactorSpaceSpecForExperiments("F").addFactors(2, 90), 2, 5);
  }

  @Test
  public void generate2_100() {
    exerciseGeneration(new FactorSpaceSpecForExperiments("F").addFactors(2, 100), 2, 5);
  }
}
