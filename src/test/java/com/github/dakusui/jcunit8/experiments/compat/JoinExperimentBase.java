package com.github.dakusui.jcunit8.experiments.compat;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.experiments.join.JoinReport;
import com.github.dakusui.jcunit8.extras.normalizer.compat.FactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CompatFactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils;
import org.junit.AfterClass;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.github.dakusui.jcunit8.experiments.join.JoinExperimentUtils.loadPregeneratedOrGenerateAndSaveCoveringArrayFor;
import static com.github.dakusui.jcunit8.testutils.UTUtils.configureStdIOs;
import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.assertCoveringArray;

public abstract class JoinExperimentBase {
  private static boolean     initialized    = false;
  private static List<Tuple> lhs            = null;
  private static FactorSpace lhsFactorSpace = null;

  @AfterClass
  public static void afterClass() {
    synchronized (JoinExperimentBase.class) {
      initialized = false;
    }
  }

  public static FactorSpaceSpecForExperiments factorSpeceSpec(String r, int numFactors) {
    return new CompatFactorSpaceSpecForExperiments(r).addFactors(2, numFactors);
  }

  @Before
  public void before() {
    synchronized (JoinExperimentRhs2_n.class) {
      if (!initialized) {
        configureStdIOs();
        FactorSpaceSpecForExperiments lhsSpec = lhsFactorSpaceSpec();
        lhsFactorSpace = lhsSpec.build();
        lhs = loadPregeneratedOrGenerateAndSaveCoveringArrayFor(
            lhsSpec,
            strength(),
            CoveringArrayGenerationUtils::generateWithIpoGplus);
        assertCoveringArray(lhs, lhsFactorSpace, strength());
        initialized = true;
      }
    }
  }

  protected abstract int strength();

  protected abstract FactorSpaceSpecForExperiments lhsFactorSpaceSpec();

  public void exerciseJoin(int times, FactorSpaceSpecForExperiments rhsSpec, List<Tuple> rhs) {
    System.out.println(JoinReport.header());
    List<Tuple> joined = null;
    for (int i = 0; i < times; i++) {
      CoveringArrayGenerationUtils.StopWatch stopWatch = new CoveringArrayGenerationUtils.StopWatch();
      joined = exerciseJoin(rhs);
      System.out.printf(
          "%s%n",
          new JoinReport(
              Integer.toString(lhsFactorSpace.getFactorNames().size()),
              Objects.toString(rhsSpec.numFactors()),
              joined.size(),
              stopWatch.get()
          )
      );
    }
    FactorSpace joinedFactorSpace = FactorSpace.create(
        new ArrayList<Factor>(lhsFactorSpaceSpec().numFactors() + rhsSpec.numFactors()) {{
          addAll(lhsFactorSpaceSpec().build().getFactors());
          addAll(rhsSpec.build().getFactors());
        }},
        Collections.emptyList()
    );
    assertCoveringArray(joined, joinedFactorSpace, strength());
  }

  private List<Tuple> exerciseJoin(List<Tuple> rhs) {
    return CoveringArrayGenerationUtils.join(
        lhs,
        rhs,
        joinerFactory(),
        strength()
    );
  }

  protected Function<Requirement, Joiner> joinerFactory() {
    return Joiner.Standard::new;
  }

}
