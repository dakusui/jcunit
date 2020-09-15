package com.github.dakusui.jcunit8.experiments.join.basic;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.extras.generators.Acts;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CompatFactorSpaceSpecForExperiments;

import java.io.File;
import java.util.List;
import java.util.function.BiFunction;

import static com.github.dakusui.jcunit8.experiments.join.JoinExperiment.loadOrGenerateCoveringArray;

public class ActsExperiment implements Experiment {
  private final int                                           strength;
  private final int                                           order;
  private final int                                           degree;
  private final BiFunction<FactorSpace, Integer, List<Tuple>> generator = (factorSpace, t) -> Acts.generateWithActs(new File("target/acts"), factorSpace, t);

  public ActsExperiment(int strength, int degree, int order) {
    this.strength = strength;
    this.order = order;
    this.degree = degree;
  }

  @Override
  public Report conduct() {
    List<Tuple> array = loadOrGenerateCoveringArray(
        new CompatFactorSpaceSpecForExperiments("L").addFactors(order, degree / 2),
        strength,
        this.generator);
    long generationTime = 0;
    return new ActsReport(array.size(), generationTime);
  }


  public static class ActsReport implements Report {
    public ActsReport(int size, long generationTime) {
    }
  }
}
