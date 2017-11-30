package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.testutils.testsuitequality.GenerationTestBase;
import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.Test;

public class GenerationExperiment extends GenerationTestBase {
  @Test
  public void generate2_10() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactors(2, 10), 2, 10);
  }

  @Test
  public void generate2_20() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactors(2, 20), 2, 10);
  }

  @Test
  public void generate2_30() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactors(2, 30), 2, 10);
  }

  @Test
  public void generate2_40() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactors(2, 40), 2, 10);
  }

  @Test
  public void generate2_50() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactors(2, 50), 2, 10);
  }

  @Test
  public void generate2_60() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactors(2, 60), 2, 5);
  }

  @Test
  public void generate2_70() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactors(2, 70), 2, 5);
  }

  @Test
  public void generate2_80() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactors(2, 80), 2, 5);
  }

  @Test
  public void generate2_90() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactors(2, 90), 2, 5);
  }

  @Test
  public void generate2_100() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactors(2, 100), 2, 5);
  }

  @Test
  public void generate10_10() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactors(10, 10), 2, 5);
  }

  @Test
  public void generate40_20() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactors(40, 20), 2, 5);
  }
}
