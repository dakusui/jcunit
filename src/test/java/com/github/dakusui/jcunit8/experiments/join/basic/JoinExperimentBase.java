package com.github.dakusui.jcunit8.experiments.join.basic;

import com.github.dakusui.jcunit8.experiments.join.acts.Acts;
import com.github.dakusui.jcunit8.experiments.join.JoinExperiment;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.function.Function;

@RunWith(Parameterized.class)
public class JoinExperimentBase {
  private final JoinExperiment experiment;

  static JoinExperiment createExperiment(int lhsNumFactors, int rhsNumFactors, int strength, Function<Requirement, Joiner> joinerFactory) {
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

  JoinExperimentBase(JoinExperiment experiment) {
    this.experiment = experiment;
  }

  @Test
  public void exercise() {
    this.experiment.exercise();
  }

  protected void joinAndPrint() {
    this.experiment.joinAndPrint();
  }
}
