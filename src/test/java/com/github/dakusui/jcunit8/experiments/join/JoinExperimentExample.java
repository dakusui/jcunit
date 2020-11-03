package com.github.dakusui.jcunit8.experiments.join;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.peerj.join.JoinExperiment;
import com.github.dakusui.peerj.join.JoinReport;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.peerj.utils.CoveringArrayGenerationUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.dakusui.peerj.utils.CoveringArrayGenerationUtils.assertCoveringArray;

public class JoinExperimentExample {
  static class JoinExperimentDummy extends JoinExperiment {
    public JoinExperimentDummy(JoinExperiment.Builder builder) {
      super(builder);
    }

    public void exercise() {
      List<Tuple> joined = null;
      List<Tuple> lhs = loadOrGenerateCoveringArray(
          this.spec.lhsSpec,
          this.spec.lhsStrength.applyAsInt(this.spec.strength),
          this.spec.generator);
      List<Tuple> rhs = loadOrGenerateCoveringArray(
          this.spec.rhsSpec,
          this.spec.rhsStrength.applyAsInt(this.spec.strength),
          this.spec.generator);
      System.out.println(JoinReport.header());
      for (int i = 0; i < spec.times; i++) {
        CoveringArrayGenerationUtils.StopWatch stopWatch = new CoveringArrayGenerationUtils.StopWatch();
        joined = exerciseJoin(lhs, rhs, this.spec.strength, this.spec.joinerFactory);
        System.out.printf(
            "%s%n",
            new JoinReport(
                formatCoveringArray(lhs, this.spec.lhsStrength, this.spec.lhsSpec),
                formatCoveringArray(rhs, this.spec.rhsStrength, this.spec.rhsSpec),
                joined.size(),
                stopWatch.get()
            )
        );
      }
      if (spec.verification) {
        FactorSpace joinedFactorSpace = FactorSpace.create(
            new ArrayList<Factor>(spec.lhsSpec.numFactors() + spec.rhsSpec.numFactors()) {{
              addAll(spec.lhsSpec.toFactorSpace().getFactors());
              addAll(spec.rhsSpec.toFactorSpace().getFactors());
            }},
            Collections.emptyList()
        );
        assertCoveringArray(joined, joinedFactorSpace, spec.strength);
      }
    }

    public static class Builder extends JoinExperiment.Builder {
      @Override
      public JoinExperimentDummy build() {
        return new JoinExperimentDummy(this.clone());
      }
    }
  }

  @Test
  public void example1() {
    ((JoinExperimentDummy) new JoinExperimentDummy.Builder()
        .lhs(new FactorSpaceSpec("L").addFactors(2, 20))
        .rhs(new FactorSpaceSpec("R").addFactors(2, 20))
        .strength(4)
        .times(10)
        .joiner(Joiner.WeakenProduct::new)
        .verification(false)
        .build()).exercise();
  }

  @Test
  public void example1_1() {
    ((JoinExperimentDummy) new JoinExperiment.Builder()
        .lhs(new FactorSpaceSpec("L").addFactors(2, 20))
        .rhs(new FactorSpaceSpec("R").addFactors(2, 20))
        .strength(5)
        .times(10)
        .joiner(Joiner.WeakenProduct::new)
        .verification(false)
        .build()).exercise();
  }

  @Test
  public void example2() {
    ((JoinExperimentDummy) new JoinExperiment.Builder()
        .lhs(new FactorSpaceSpec("L").addFactors(2, 20), 4)
        .rhs(new FactorSpaceSpec("R").addFactors(2, 20), 4)
        .strength(3)
        .times(2)
        .joiner(Joiner.Standard::new)
        .verification(false)
        .build()).exercise();
  }
}
