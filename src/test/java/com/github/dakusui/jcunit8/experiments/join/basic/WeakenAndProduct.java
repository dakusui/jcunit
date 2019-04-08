package com.github.dakusui.jcunit8.experiments.join.basic;

import com.github.dakusui.jcunit8.experiments.join.ActsUtils;
import com.github.dakusui.jcunit8.experiments.join.JoinExperiment;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

@RunWith(Parameterized.class)
public class WeakenAndProduct {
  private final JoinExperiment experiment;

  @Parameters
  public static List<JoinExperiment> experiments() {
    List<JoinExperiment> work = new LinkedList<>();
    for (int t = 2; t <= 3; t++)
      for (int i = 10; i < 100; i += 10)
        for (int j = 10; j < 100; j += 10)
          work.add(createExperiment(i, j, t));
    work.sort((o1, o2) -> (o1.cost() - o2.cost()));
    return work;
  }

  private static JoinExperiment createExperiment(int lhsNumFactors, int rhsNumFactors, int strength) {
    return new JoinExperiment.Builder()
        .lhs(new FactorSpaceSpec("L").addFactor(2, lhsNumFactors))
        .rhs(new FactorSpaceSpec("R").addFactor(2, rhsNumFactors))
        .strength(strength)
        .times(2)
        .joiner(Joiner.WeakenProduct::new)
        .generator((factorSpace, t) -> ActsUtils.generateWithActs(new File("target/acts"), factorSpace, t))
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
