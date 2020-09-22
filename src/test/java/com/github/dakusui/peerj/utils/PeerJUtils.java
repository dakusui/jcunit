package com.github.dakusui.peerj.utils;

import com.github.dakusui.peerj.ConstraintSet;
import com.github.dakusui.peerj.Experiment;
import com.github.dakusui.peerj.acts.Acts;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import com.github.dakusui.peerj.acts.ActsExperiment;
import com.github.dakusui.peerj.join.JoinExperiment;
import com.github.dakusui.peerj.model.CompatFactorSpaceSpecForExperiments;
import com.github.dakusui.peerj.model.FactorSpaceSpecForExperiments;

import java.io.File;

public enum PeerJUtils {
  ;

  public static Experiment createExperiment(int strength, int degree, int order, GenerationMode generationMode, ConstraintSet constraintSet) {
    return generationMode.createExperiment(strength, degree, order, constraintSet);
  }

  public enum GenerationMode {
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
        return new ActsExperiment(strength, degree, order, constraintSet);
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
        FactorSpaceSpecForExperiments factorSpaceSpec = this.constraintSetName(constraintSet.name());
        for (int offset = 0; offset < degree; offset += 10)
          constraintSet.constraintFactory(offset).ifPresent(factorSpaceSpec::addConstraint);
      }};
    }
  }
}
