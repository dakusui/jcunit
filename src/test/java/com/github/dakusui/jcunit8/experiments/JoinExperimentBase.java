package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils;
import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.AfterClass;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.github.dakusui.jcunit8.testutils.UTUtils.configureStdIOs;
import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.assertCoveringArray;
import static com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec.loadPregeneratedOrGenerateAndSaveCoveringArrayFor;

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

  static FactorSpaceSpec factorSpeceSpec(String r, int numFactors) {
    return new FactorSpaceSpec(r).addFactor(2, numFactors);
  }

  @Before
  public void before() {
    synchronized (JoinExperimentRhs2_n.class) {
      if (!initialized) {
        configureStdIOs();
        FactorSpaceSpec lhsSpec = lhsFactorSpaceSpec();
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

  protected abstract FactorSpaceSpec lhsFactorSpaceSpec();

  void exerciseJoin(int times, FactorSpaceSpec rhsSpec, List<Tuple> rhs) {
    System.out.println(Report.header());
    List<Tuple> joined = null;
    for (int i = 0; i < times; i++) {
      CoveringArrayGenerationUtils.StopWatch stopWatch = new CoveringArrayGenerationUtils.StopWatch();
      joined = exerciseJoin(rhs);
      System.out.printf(
          "%s%n",
          new Report(
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

  static class Report {
    private final String lhsDesc;
    private final String rhsDesc;
    private final long   time;
    private final int    size;

    Report(String lhsDesc, String rhdDesc, int size, long time) {
      this.lhsDesc = lhsDesc;
      this.rhsDesc = rhdDesc;
      this.size = size;
      this.time = time;
    }

    static String header() {
      return "lhs,rhs,time,size";
    }

    public String toString() {
      return String.format("%s,%s,%s,%s", lhsDesc, rhsDesc, time, size);
    }
  }
}
