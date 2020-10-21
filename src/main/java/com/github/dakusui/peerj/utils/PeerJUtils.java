package com.github.dakusui.peerj.utils;

import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.peerj.acts.Acts;
import com.github.dakusui.peerj.acts.ActsExperiment;
import com.github.dakusui.peerj.join.JoinExperiment;
import com.github.dakusui.peerj.model.ConstraintSet;
import com.github.dakusui.peerj.model.Experiment;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public enum PeerJUtils {
  ;
  private static final Logger LOGGER = LoggerFactory.getLogger(PeerJUtils.class);

  public static Experiment createExperiment(int strength, int degree, int order, GenerationMode generationMode, ConstraintSet constraintSet, ActsExperiment.ActsOpts actsOpts) {
    return generationMode.createExperiment(strength, degree, order, constraintSet, actsOpts);
  }

  public static File createTempDirectory(String pathname) {
    try {
      File dir = new File(pathname);
      LOGGER.debug("{} was created={}", dir, dir.mkdirs());
      return Files.createTempDirectory(dir.toPath(), "jcunit-").toFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public enum GenerationMode {
    WITH_JOIN {
      @Override
      JoinExperiment createExperiment(int strength, int degree, int order, ConstraintSet constraintSet, ActsExperiment.ActsOpts opts) {
        createTempDirectory("target/acts");
        int lhsDegree = degree / 2;
        int rhsDegree = degree / 2;
        return new JoinExperiment.Builder()
            .lhs(createFactorySpaceSpec(constraintSet, "L", lhsDegree).addFactors(order, lhsDegree))
            .rhs(createFactorySpaceSpec(constraintSet, "R", rhsDegree).addFactors(order, rhsDegree))
            .strength(strength)
            .times(1)
            .joiner(Joiner.WeakenProduct::new)
            .generator((baseDir, factorSpace, t) -> Acts.generateWithActs(baseDir, factorSpace, t, opts.algorithm, opts.constraintHandling))
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

    abstract Experiment createExperiment(int strength, int degree, int order, ConstraintSet constraintSet, ActsExperiment.ActsOpts actsOpts);

    static FactorSpaceSpec createFactorySpaceSpec(ConstraintSet constraintSet, final String prefix, int degree) {
      return new FactorSpaceSpec(prefix) {{
        FactorSpaceSpec factorSpaceSpec = this.constraintSetName(constraintSet.name());
        for (int offset = 0; offset < degree; offset += 10)
          constraintSet.constraintFactory(offset).ifPresent(factorSpaceSpec::addConstraint);
      }};
    }
  }

}
