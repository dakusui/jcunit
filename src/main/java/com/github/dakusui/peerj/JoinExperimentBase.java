package com.github.dakusui.peerj;

import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.peerj.acts.Acts;
import com.github.dakusui.peerj.join.JoinExperiment;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.peerj.utils.PeerJUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.function.Function;

@RunWith(Parameterized.class)
public class JoinExperimentBase {
  private final Experiment experiment;

  protected static JoinExperiment createExperiment(int lhsNumFactors, int rhsNumFactors, int strength, Function<Requirement, Joiner> joinerFactory) {
    PeerJUtils.createTempDirectory("target/acts");
    return new JoinExperiment.Builder()
        .lhs(new FactorSpaceSpec("L").addFactors(2, lhsNumFactors))
        .rhs(new FactorSpaceSpec("R").addFactors(2, rhsNumFactors))
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
