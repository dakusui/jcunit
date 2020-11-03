package com.github.dakusui.peerj.acts;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.experiments.join.acts.ActsUtilsTest;
import com.github.dakusui.peerj.join.JoinExperiment;
import com.github.dakusui.peerj.model.ConstraintSet;
import com.github.dakusui.peerj.model.Experiment;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.peerj.utils.JoinExperimentUtils;

import java.util.List;

import static com.github.dakusui.peerj.join.JoinExperiment.loadOrGenerateCoveringArray;

public class ActsExperiment implements Experiment {
  private final int                      strength;
  private final int                      order;
  private final int                      degree;
  private final JoinExperiment.Generator generator;
  private final ConstraintSet            constraintSet;

  public ActsExperiment(int strength, int degree, int order, ConstraintSet constraintSet, ActsOpts actsOpts) {
    this.strength = strength;
    this.order = order;
    this.degree = degree;
    this.constraintSet = constraintSet;
    this.generator = (baseDir, factorSpace, strength1) -> ActsUtilsTest.generateWithActs(baseDir, factorSpace, strength1, actsOpts.algorithm, actsOpts.constraintHandling);
  }

  @Override
  public Report conduct() {
    FactorSpaceSpec abstractModel = new FactorSpaceSpec("L") {{
      FactorSpaceSpec factorSpaceSpec = this.constraintSetName(constraintSet.name());
      for (int offset = 0; offset < degree; offset += 10)
        constraintSet.constraintFactory(offset).ifPresent(factorSpaceSpec::addConstraint);
    }}.addFactors(order, degree);
    List<Tuple> array = loadOrGenerateCoveringArray(
        abstractModel,
        this.strength,
        this.generator);
    long generationTime = JoinExperimentUtils.timeSpentForGeneratingCoveringArray(abstractModel, strength, this.generator);
    return new ActsReport(array.size(), generationTime, strength, order, degree);
  }


  public static class ActsOpts {
    public final String algorithm;
    public final String constraintHandling;

    public ActsOpts(String algorithm, String constraintHandling) {
      this.algorithm = algorithm;
      this.constraintHandling = constraintHandling;
    }
  }
}
