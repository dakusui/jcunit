package com.github.dakusui.peerj.acts;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.peerj.model.ConstraintSet;
import com.github.dakusui.peerj.utils.JoinExperimentUtils;
import com.github.dakusui.peerj.model.Experiment;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.io.File;
import java.util.List;
import java.util.function.BiFunction;

import static com.github.dakusui.peerj.join.JoinExperiment.loadOrGenerateCoveringArray;

public class ActsExperiment implements Experiment {
  private final int                                           strength;
  private final int                                           order;
  private final int                                           degree;
  private final BiFunction<FactorSpace, Integer, List<Tuple>> generator = (factorSpace, t) -> Acts.generateWithActs(new File("target/acts"), factorSpace, t);
  private final ConstraintSet                                 constraintSet;

  public ActsExperiment(int strength, int degree, int order, ConstraintSet constraintSet) {
    this.strength = strength;
    this.order = order;
    this.degree = degree;
    this.constraintSet = constraintSet;
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
        strength,
        this.generator);
    long generationTime = JoinExperimentUtils.timeSpentForGeneratingCoveringArray(abstractModel, strength, this.generator);
    return new ActsReport(array.size(), generationTime, strength, order, degree);
  }


}
