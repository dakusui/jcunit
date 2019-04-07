package com.github.dakusui.jcunit8.experiments.compat;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils;

import java.util.List;

import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.assertCoveringArray;
import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.createFactorSpace;
import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.generateWithIpoGplus;
import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.join;
import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.mergeFactorSpaces;

public enum CompatJoinExperimentUtils {
  ;

  public static void exerciseStandardExperiment10Times(int numLhsFactors, int numRhsFactors) {
    // warm up
    exercise(2, 2, numLhsFactors);
    for (int j = 0; j < 10; j++) {
      System.out.println(exercise(2, 2, numLhsFactors, numRhsFactors));
    }
  }

  public static CompatJoinReport exercise(int strength, int numLevels, int numFactors) {
    return exercise(strength, numLevels, numFactors, numFactors);
  }

  public static CompatJoinReport exercise(int strength, int numLevels, int numFactorsLhs, int numFactorsRhs) {
    CompatJoinReport.Builder reportBuilder = new CompatJoinReport.Builder(numFactorsLhs, numFactorsRhs);
    CoveringArrayGenerationUtils.StopWatch stopWatch = new CoveringArrayGenerationUtils.StopWatch();

    FactorSpace lhsFactorSpace = createFactorSpace("F", numLevels, numFactorsLhs);
    List<Tuple> lhs = generateWithIpoGplus(
        lhsFactorSpace,
        strength
    );
    reportBuilder = reportBuilder.timeLhs(stopWatch.get()).sizeLhs(lhs.size());

    FactorSpace rhsFactorSpace = createFactorSpace("G", numLevels, numFactorsRhs);
    List<Tuple> rhs = generateWithIpoGplus(
        rhsFactorSpace,
        strength
    );
    reportBuilder = reportBuilder.timeRhs(stopWatch.get()).sizeRhs(rhs.size());

    FactorSpace mergedFactorSpace = mergeFactorSpaces(lhsFactorSpace, rhsFactorSpace);
    List<Tuple> merged = generateWithIpoGplus(
        mergedFactorSpace,
        strength
    );
    reportBuilder = reportBuilder.timeMerged(stopWatch.get()).sizeMerged(merged.size());
    List<Tuple> joined = join(lhs, rhs, Joiner.Standard::new, strength);
    reportBuilder = reportBuilder.timeJoining(stopWatch.get()).sizeJoining(joined.size());

    assertCoveringArray(lhs, lhsFactorSpace, strength);
    assertCoveringArray(rhs, rhsFactorSpace, strength);
    assertCoveringArray(merged, mergedFactorSpace, strength);
    assertCoveringArray(joined, mergedFactorSpace, strength);

    return reportBuilder.build();
  }
}
