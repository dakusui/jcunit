package com.github.dakusui.jcunit8.testutils.testsuitequality;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import org.junit.BeforeClass;

import java.util.List;

import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.assertCoveringArray;

public class GenerationTestBase {
  @BeforeClass
  public static void beforeClass() {
    System.out.println(Report.header());
  }


  protected void exerciseGeneration(FactorSpaceSpec factorSpaceSpec, int strength, int times) {
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
    CoveringArrayGenerationUtils.StopWatch stopWatch = new CoveringArrayGenerationUtils.StopWatch();
    return new Report(
        String.format("numFactors=%d", factorSpace.getFactors().size()),
        CoveringArrayGenerationUtils.generateWithIpoGplus(factorSpace, strength),
        stopWatch.get()
    );
  }

  public static class Report {
    private final String      factorSpaceDesc;
    private final int         size;
    private final long        time;
    public final  List<Tuple> generatedCoveringArray;

    public Report(String factorSpaceDesc, List<Tuple> generatedCoveringArray, long time) {
      this.factorSpaceDesc = factorSpaceDesc;
      this.generatedCoveringArray = generatedCoveringArray;
      this.size = generatedCoveringArray.size();
      this.time = time;
    }

    @Override
    public String toString() {
      return String.format("%s,%s,%s", this.factorSpaceDesc, size, time);
    }

    public static String header() {
      return "factorSpace,size,time";
    }
  }
}
