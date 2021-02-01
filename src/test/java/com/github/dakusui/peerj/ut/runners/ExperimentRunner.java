package com.github.dakusui.peerj.ut.runners;

import com.github.dakusui.jcunit8.tests.components.ext.ActsUtilsTest;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.peerj.join.JoinExperiment;
import com.github.dakusui.peerj.model.Experiment;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.peerj.utils.PeerJUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Function;

@RunWith(Parameterized.class)
public class ExperimentRunner {
  private final Experiment experiment;

  protected static JoinExperiment createExperiment(int lhsNumFactors, int rhsNumFactors, int strength, Function<Requirement, Joiner> joinerFactory) {
    PeerJUtils.createTempDirectory("target/acts");
    return new JoinExperiment.Builder()
        .lhs(new FactorSpaceSpec("L").addFactors(2, lhsNumFactors))
        .rhs(new FactorSpaceSpec("R").addFactors(2, rhsNumFactors))
        .strength(strength)
        .times(2)
        .joiner(joinerFactory)
        .generator((baseDir, factorSpace, t) -> ActsUtilsTest.generateWithActs(baseDir, factorSpace, t, "ipog", "solver"))
        .verification(false)
        .build();
  }

  public ExperimentRunner(Experiment experiment) {
    this.experiment = experiment;
  }

  @Test
  public void exercise() {
    System.out.println(this.experiment.conduct());
  }
}
