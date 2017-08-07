package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static com.github.dakusui.jcunit8.experiments.JoinExperimentUtils.assertCoveringArray;

public class GenerationExperiment {
  static class Report {
    private final String      factorSpaceDesc;
    private final int         size;
    private final long        time;
    private final List<Tuple> generatedCoveringArray;

    Report(String factorSpaceDesc, List<Tuple> generatedCoveringArray, long time) {
      this.factorSpaceDesc = factorSpaceDesc;
      this.generatedCoveringArray = generatedCoveringArray;
      this.size = generatedCoveringArray.size();
      this.time = time;
    }

    @Override
    public String toString() {
      return String.format("%s,%s,%s", this.factorSpaceDesc, size, time);
    }

    static String header() {
      return "factorSpace,size,time";
    }
  }

  @BeforeClass
  public static void beforeClass() {
    System.out.println(Report.header());
  }

  @Test
  public void generate3_3() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactor(3, 3), 2, 10);
  }

  @Test
  public void generate2_10() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactor(2, 10), 2, 10);
  }

  @Test
  public void generate2_20() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactor(2, 20), 2, 10);
  }

  @Test
  public void generate2_30() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactor(2, 30), 2, 10);
  }

  @Test
  public void generate2_40() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactor(2, 40), 2, 10);
  }

  @Test
  public void generate2_50() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactor(2, 50), 2, 10);
  }

  @Test
  public void generate2_60() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactor(2, 60), 2, 5);
  }

  @Test
  public void generate2_70() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactor(2, 70), 2, 5);
  }

  @Test
  public void generate2_80() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactor(2, 80), 2, 5);
  }

  @Test
  public void generate2_90() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactor(2, 90), 2, 5);
  }

  @Test
  public void generate2_100() {
    exerciseGeneration(new FactorSpaceSpec("F").addFactor(2, 100), 2, 5);
  }

  void exerciseGeneration(FactorSpaceSpec factorSpaceSpec, int strength, int times) {
    assertCoveringArray(
        exerciseGeneration(factorSpaceSpec, strength).generatedCoveringArray,
        factorSpaceSpec.build(),
        strength
    );
    for (int i = 0; i < times; i++) {
      System.out.println(exerciseGeneration(factorSpaceSpec, strength));
    }
  }

  private Report exerciseGeneration(FactorSpaceSpec factorSpaceSpec, int strength) {
    FactorSpace factorSpace = factorSpaceSpec.build();
    JoinExperimentUtils.StopWatch stopWatch = new JoinExperimentUtils.StopWatch();
    return new Report(
        String.format("numFactors=%d", factorSpace.getFactors().size()),
        CoveringArrayGenerationUtils.generateWithIpoGplus(factorSpace, strength),
        stopWatch.get()
    );
  }
}
