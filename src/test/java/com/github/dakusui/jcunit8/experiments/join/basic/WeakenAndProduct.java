package com.github.dakusui.jcunit8.experiments.join.basic;

import com.github.dakusui.jcunit8.experiments.join.JoinExperiment;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.List;

import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public class WeakenAndProduct {
  private final JoinExperiment experiment;

  @Parameters
  public static List<JoinExperiment> experiments() {
    return asList(
        createExperiment(10, 10, 3),
        createExperiment(20, 10, 3),
        createExperiment(30, 10, 3),
        createExperiment(40, 10, 3),
        createExperiment(50, 10, 3),
        createExperiment(60, 10, 3),
        createExperiment(70, 0, 3),
        createExperiment(80, 10, 3),
        createExperiment(90, 10, 3),
        createExperiment(100, 10, 3),
        createExperiment(10, 20, 3),
        createExperiment(20, 20, 3),
        createExperiment(30, 20, 3),
        createExperiment(40, 20, 3),
        createExperiment(50, 20, 3),
        createExperiment(60, 20, 3),
        createExperiment(70, 20, 3),
        createExperiment(80, 20, 3),
        createExperiment(90, 20, 3),
        createExperiment(100, 20, 3)
    );
  }

  private static JoinExperiment createExperiment(int lhsNumFactors, int rhsNumFactors, int strength) {
    return new JoinExperiment.Builder()
        .lhs(new FactorSpaceSpec("L").addFactor(2, lhsNumFactors))
        .rhs(new FactorSpaceSpec("R").addFactor(2, rhsNumFactors))
        .strength(strength)
        .times(2)
        .joiner(Joiner.WeakenProduct::new)
        .verification(false)
        .build();
  }

  public WeakenAndProduct(JoinExperiment experiment) {
    this.experiment = experiment;
  }

  @Test
  public void exercise() {
    this.experiment.exercise();
  }
}
