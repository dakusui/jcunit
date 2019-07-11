package com.github.dakusui.jcunit8.experiments.compat;

import com.github.dakusui.jcunit8.testutils.testsuitequality.CompatFactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.testutils.testsuitequality.GenerationTestBase;
import org.junit.Test;

public class StandardFactorSpaces extends GenerationTestBase {
  @Test
  public void generate3_4() {
    exerciseGeneration(
        new CompatFactorSpaceSpecForExperiments("F1")
            .addFactors(3, 4),
        2,
        1);
  }

  @Test
  public void generate3_13() {
    exerciseGeneration(
        new CompatFactorSpaceSpecForExperiments("F2")
            .addFactors(3, 13),
        2,
        1);
  }

  @Test
  public void generate4_15$3_17$2_29() {
    exerciseGeneration(
        new CompatFactorSpaceSpecForExperiments("F3")
            .addFactors(4, 15)
            .addFactors(3, 17)
            .addFactors(2, 29),
        2,
        1);
  }

  @Test
  public void generate4_1$3_39$2_35() {
    exerciseGeneration(
        new CompatFactorSpaceSpecForExperiments("F4")
            .addFactors(4, 1)
            .addFactors(3, 39)
            .addFactors(2, 35),
        2,
        1);
  }

  @Test
  public void generate2_100() {
    exerciseGeneration(
        new CompatFactorSpaceSpecForExperiments("F5")
            .addFactors(2, 100),
        2,
        1);
  }

  @Test
  public void generate10_20() {
    exerciseGeneration(
        new CompatFactorSpaceSpecForExperiments("F6")
            .addFactors(10, 20),
        2,
        1);
  }
}
