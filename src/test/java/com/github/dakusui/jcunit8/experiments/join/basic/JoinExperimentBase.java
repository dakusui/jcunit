package com.github.dakusui.jcunit8.experiments.join.basic;

import com.github.dakusui.peerj.Experiment;
import com.github.dakusui.peerj.JoinExperiment;
import com.github.dakusui.peerj.acts.Acts;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import com.github.dakusui.peerj.model.CompatFactorSpaceSpecForExperiments;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.function.Function;

@RunWith(Parameterized.class)
public class JoinExperimentBase {
  private final Experiment experiment;

  static JoinExperiment createExperiment(int lhsNumFactors, int rhsNumFactors, int strength, Function<Requirement, Joiner> joinerFactory) {
    UTUtils.createTempDirectory("target/acts");
    return new JoinExperiment.Builder()
        .lhs(new CompatFactorSpaceSpecForExperiments("L").addFactors(2, lhsNumFactors))
        .rhs(new CompatFactorSpaceSpecForExperiments("R").addFactors(2, rhsNumFactors))
        .strength(strength)
        .times(2)
        .joiner(joinerFactory)
        .generator((factorSpace, t) -> Acts.generateWithActs(new File("target/acts"), factorSpace, t))
        .verification(false)
        .build();
  }

  public JoinExperimentBase(Experiment experiment) {
    this.experiment = experiment;
  }

  @Test
  public void exercise() {
    System.out.println(this.experiment.conduct());
  }

}
