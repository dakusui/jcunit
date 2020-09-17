package com.github.dakusui.jcunit8.experiments.join.basic;

import com.github.dakusui.jcunit8.experiments.generation.ConstraintSet;
import com.github.dakusui.jcunit8.experiments.join.JoinExperiment;
import com.github.dakusui.jcunit8.extras.generators.Acts;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CompatFactorSpaceSpecForExperiments;
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

  JoinExperimentBase(Experiment experiment) {
    this.experiment = experiment;
  }

  @Test
  public void exercise() {
    System.out.println(this.experiment.conduct());
  }

  static Experiment createExperiment(int strength, int degree, int order, GenerationMode generationMode, ConstraintSet constraintSet) {
    return generationMode.createExperiment(strength, degree, order, constraintSet);
  }

  enum GenerationMode {
    WITH_JOIN {
      @Override
      JoinExperiment createExperiment(int strength, int degree, int order, ConstraintSet constraintSet) {
        UTUtils.createTempDirectory("target/acts");
        int lhsDegree = degree / 2;
        int rhsDegree = degree / 2;
        return new JoinExperiment.Builder()
            .lhs(createFactorySpaceSpec(constraintSet, "L", lhsDegree).addFactors(order, lhsDegree))
            .rhs(createFactorySpaceSpec(constraintSet, "R", rhsDegree).addFactors(order, rhsDegree))
            .strength(strength)
            .times(1)
            .joiner(Joiner.WeakenProduct::new)
            .generator((factorSpace, t) -> Acts.generateWithActs(new File("target/acts"), factorSpace, t))
            .verification(false)
            .build();
      }
    },
    WITH_ACTS_FULL {
      @Override
      Experiment createExperiment(int strength, int degree, int order, ConstraintSet constraintSet) {
        return new ActsExperiment(strength, degree, order);
      }
    },
    WITH_ACTS_INCREMENTAL {
      @Override
      JoinExperiment createExperiment(int strength, int degree, int order, ConstraintSet constraintSet) {
        return null;
      }
    };

    abstract Experiment createExperiment(int strength, int degree, int order, ConstraintSet constraintSet);

    static CompatFactorSpaceSpecForExperiments createFactorySpaceSpec(ConstraintSet constraintSet, final String prefix, int degree) {
      return new CompatFactorSpaceSpecForExperiments(prefix) {{
        for (int offset = 0; offset < degree; offset += 10)
          constraintSet.constraintFactories(offset).ifPresent(this::addConstraint);
      }};
    }
  }
}
