package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import com.github.dakusui.jcunit8.testutils.testsuitequality.GenerationTestBase;
import org.junit.Test;

public class StandardFactorSpaces extends GenerationTestBase {
  @Test
  public void generate3_4() {
    exerciseGeneration(
        new FactorSpaceSpec("F1")
            .addFactor(3, 4),
        2,
        1);
  }

  @Test
  public void generate3_13() {
    exerciseGeneration(
        new FactorSpaceSpec("F2")
            .addFactor(3, 13),
        2,
        1);
  }

  @Test
  public void generate4_15$3_17$2_20() {
    exerciseGeneration(
        new FactorSpaceSpec("F3")
            .addFactor(4, 15)
            .addFactor(3, 17)
            .addFactor(2, 20),
        2,
        1);
  }

  @Test
  public void generate4_1$3_30$2_35() {
    exerciseGeneration(
        new FactorSpaceSpec("F4")
            .addFactor(4, 1)
            .addFactor(3, 30)
            .addFactor(2, 35),
        2,
        1);
  }

  @Test
  public void generate2_100() {
    exerciseGeneration(
        new FactorSpaceSpec("F5")
            .addFactor(2, 100),
        2,
        1);
  }

  @Test
  public void generate10_20() {
    exerciseGeneration(
        new FactorSpaceSpec("F6")
            .addFactor(10, 20),
        2,
        1);
  }
}
