package com.github.dakusui.peerj.runners;

import com.github.dakusui.jcunit8.experiments.join.acts.ActsUtilsTest;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.peerj.acts.ActsExperiment;
import com.github.dakusui.peerj.join.JoinExperiment;
import com.github.dakusui.peerj.model.ConstraintSet;
import com.github.dakusui.peerj.model.Experiment;
import com.github.dakusui.peerj.utils.PeerJUtils;
import org.junit.runners.Parameterized.Parameters;

import java.util.LinkedList;
import java.util.List;

public class PeerJRunner extends ExperimentRunner {
  public PeerJRunner(Experiment experiment) {
    super(experiment);
  }

  @Parameters
  public static List<Experiment> experiments() {
    List<Experiment> work = new LinkedList<>();
    for (ConstraintSet constraintSet : new ConstraintSet[] { ConstraintSet.BASIC_PLUS, ConstraintSet.BASIC, ConstraintSet.NONE })
      for (GenerationMode generationMode : new GenerationMode[] { GenerationMode.WITH_ACTS_FULL, GenerationMode.WITH_JOIN })
        for (int t = 2; t <= 3; t++)
          for (int i = 20; i < 60; i += 20)
            work.add(GenerationMode.createExperiment(t, i, 4, generationMode, constraintSet, new ActsExperiment.ActsOpts("ipog", "solver")));
    return work;
  }

  public enum GenerationMode {
    WITH_JOIN {
      @Override
      JoinExperiment createExperiment(int strength, int degree, int order, ConstraintSet constraintSet, ActsExperiment.ActsOpts opts) {
        PeerJUtils.createTempDirectory("target/acts");
        int lhsDegree = degree / 2;
        int rhsDegree = degree / 2;
        return new JoinExperiment.Builder()
            .lhs(PeerJUtils.createFactorySpaceSpec(constraintSet, "L", lhsDegree, -1, -1).addFactors(order, lhsDegree))
            .rhs(PeerJUtils.createFactorySpaceSpec(constraintSet, "R", rhsDegree, -1, -1).addFactors(order, rhsDegree))
            .strength(strength)
            .times(1)
            .joiner(Joiner.WeakenProduct::new)
            .generator((baseDir, factorSpace, t) -> ActsUtilsTest.generateWithActs(baseDir, factorSpace, t, opts.algorithm, opts.constraintHandling))
            .verification(false)
            .build();
      }
    },
    WITH_ACTS_FULL {
      @Override
      Experiment createExperiment(int strength, int degree, int order, ConstraintSet constraintSet, ActsExperiment.ActsOpts actsOpts) {
        return new ActsExperiment(strength, degree, order, constraintSet, actsOpts);
      }
    },
    WITH_ACTS_INCREMENTAL {
      @Override
      JoinExperiment createExperiment(int strength, int degree, int order, ConstraintSet constraintSet, ActsExperiment.ActsOpts actsOpts) {
        throw new UnsupportedOperationException();
      }
    };

    public static Experiment createExperiment(int strength, int degree, int order, GenerationMode generationMode, ConstraintSet constraintSet, ActsExperiment.ActsOpts actsOpts) {
      return generationMode.createExperiment(strength, degree, order, constraintSet, actsOpts);
    }

    abstract Experiment createExperiment(int strength, int degree, int order, ConstraintSet constraintSet, ActsExperiment.ActsOpts actsOpts);
  }
}
