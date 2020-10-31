package com.github.dakusui.peerj;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.peerj.model.ConstraintSet;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.peerj.utils.PeerJUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.PeerJExperimentBase.ConstraintHandlingMethod.FORBIDDEN_TUPLES;
import static com.github.dakusui.peerj.PeerJExperimentBase.ConstraintHandlingMethod.SOLVER;
import static com.github.dakusui.peerj.PeerJUtils2.*;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MINUTES;

@RunWith(Enclosed.class)
public class IndustrialSimulationSuite {
  @RunWith(Parameterized.class)
  public abstract static class PeerJExperimentParameterized extends PeerJExperimentBase {
    public static class Spec extends PeerJExperimentBase.Spec {
      final String      factorSpaceName;
      final FactorSpace factorSpace;

      public Spec(String factorSpaceName, FactorSpace factorSpace, int strength, Algorithm algorithm, ConstraintHandlingMethod constraintHandlingMethod) {
        super(strength, algorithm, constraintHandlingMethod);
        this.factorSpace = factorSpace;
        this.factorSpaceName = factorSpaceName;
      }

      @Override
      public String toString() {
        return format("%s:%s", this.factorSpaceName, super.toString());
      }

      public static class Builder extends PeerJExperimentBase.Spec.Builder<Builder> {
        private int           degree;
        private int           rank;
        private ConstraintSet constraintSet;
        private String        prefix;

        public Builder() {
          this.prefix("prefix").degree(100).rank(2).constraintSet(ConstraintSet.BASIC);
        }

        public Builder degree(int degree) {
          this.degree = degree;
          return this;
        }

        public Builder rank(int rank) {
          this.rank = rank;
          return this;
        }

        public Builder constraintSet(ConstraintSet constraintSet) {
          this.constraintSet = requireNonNull(constraintSet);
          return this;
        }

        public Builder prefix(String prefix) {
          this.prefix = prefix;
          return this;
        }

        @Override
        public Spec build() {
          FactorSpaceSpec factorySpaceSpec = PeerJUtils.createFactorySpaceSpec(this.constraintSet, this.prefix, this.degree);
          for (int i = 0; i < this.degree; i++)
            factorySpaceSpec.addFactor(this.rank);
          return new Spec(
              factorySpaceSpec.createSignature(),
              factorySpaceSpec.toFactorSpace(),
              this.strength,
              this.algorithm,
              this.constraintHandlingMethod
          );
        }
      }
    }

    public final  Spec        spec;
    private final FactorSpace factorSpace;
    private final String      dataSetName;

    public PeerJExperimentParameterized(Spec spec) {
      this.spec = spec;
      this.factorSpace = spec.factorSpace;
      this.dataSetName = spec.factorSpaceName;
    }

    @Override
    protected ConstraintHandlingMethod constraintHandlingMethod() {
      return spec.constraintHandlingMethod;
    }

    @Override
    protected Algorithm algorithm() {
      return spec.algorithm;
    }

    @Override
    protected int strength() {
      return spec.strength;
    }

    protected FactorSpace factorSpace() {
      return this.factorSpace;
    }

    protected String dataSetName() {
      return this.dataSetName;
    }

    @Test
    public void acts() {
      String dataSetName = this.dataSetName();
      int strength = strength();
      String generationMode = "acts";
      String partitionerName = "none";
      File baseDir = baseDirFor(dataSetName, this.spec.strength, generationMode, partitionerName);
      FactorSpace factorSpace = this.factorSpace();
      StopWatch<PeerJExperimentParameterized, List<Tuple>> stopWatch = new StopWatch<>(
          Printable.function("conductActsExperiment", (PeerJExperimentParameterized self) -> generateWithActs(baseDir, factorSpace, strength, algorithm(), constraintHandlingMethod())),
          (PeerJExperimentParameterized self) -> format("[%s]", self.spec),
          (List<Tuple> result) -> format("[size:%s]", result.size()));
      try {
        stopWatch.apply(this);
      } finally {
        writeTo(resultFile(dataSetName, strength(), generationMode, partitionerName), Stream.of(stopWatch.report()));
      }
    }

    @Test
    public void join_weakenProduct() {
      String dataSetName = this.dataSetName();
      int strength = strength();
      String generationMode = "join";
      Partitioner partitioner = evenPartitioner();
      File baseDir = baseDirFor(dataSetName, this.spec.strength, generationMode, partitioner.name());
      FactorSpace factorSpace = this.factorSpace();
      Requirement requirement = requirement(strength);
      StopWatch<PeerJExperimentParameterized, List<Tuple>> stopWatch = new StopWatch<>(
          Printable.function("conductJoinExperiment", (PeerJExperimentParameterized self) -> generateWithCombinatorialJoin(requirement, baseDir, partitioner, factorSpace, algorithm(), constraintHandlingMethod(), "")),
          (PeerJExperimentParameterized self) -> format("[%s]", self.spec),
          (List<Tuple> result) -> format("[size:%s]", result.size()));
      try {
        stopWatch.apply(this);
      } finally {
        writeTo(resultFile(dataSetName, strength(), generationMode, partitioner.name()), Stream.of(stopWatch.report()));
      }
    }
  }

  public static class Strength2 extends PeerJExperimentParameterized {
    public Strength2(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parametersWith(2, SOLVER);
    }
  }

  private static List<PeerJExperimentParameterized.Spec> parametersWith(int strength, PeerJExperimentBase.ConstraintHandlingMethod constraintHandlingMethod) {
    return Arrays.asList(
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(20).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(40).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(60).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(80).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(100).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(120).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(140).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(180).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(180).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(200).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(20).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(40).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(60).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(80).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(100).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(120).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(140).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(160).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(180).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(200).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(20).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(40).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(60).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(80).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(100).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(120).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(140).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(160).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(180).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(constraintHandlingMethod).build(),
        new PeerJExperimentParameterized.Spec.Builder().strength(strength).degree(200).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(constraintHandlingMethod).build()
    );
  }

  public static class Strength2ForbiddenTuples extends PeerJExperimentParameterized {
    public Strength2ForbiddenTuples(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parametersWith(2, FORBIDDEN_TUPLES);
    }
  }

  public static class Strength3 extends PeerJExperimentParameterized {
    public static final int T = 3;

    public Strength3(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parametersWith(T, SOLVER);
    }

    public static class Strength3ForbiddenTuples extends PeerJExperimentParameterized {
      public static final int T = 3;

      public Strength3ForbiddenTuples(Spec spec) {
        super(spec);
      }

      @Parameters
      public static List<Spec> parameters() {
        return parametersWith(T, FORBIDDEN_TUPLES);
      }

      public static class Strength4 extends PeerJExperimentParameterized {
        private static final int T = 4;

        public Strength4(Spec spec) {
          super(spec);
        }

        @Parameters
        public static List<Spec> parameters() {
          return Arrays.asList(
              new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(60).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(60).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(60).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(SOLVER).build()
          );
        }
      }

      public static class Strength4ForbiddenTuples extends PeerJExperimentParameterized {
        private static final int T = 4;

        public Strength4ForbiddenTuples(Spec spec) {
          super(spec);
        }

        @Parameters
        public static List<Spec> parameters() {
          return Arrays.asList(
              new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(FORBIDDEN_TUPLES).build(),
              new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(FORBIDDEN_TUPLES).build(),
              new Spec.Builder().strength(T).degree(60).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(FORBIDDEN_TUPLES).build(),
              new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(FORBIDDEN_TUPLES).build(),
              new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(FORBIDDEN_TUPLES).build(),
              new Spec.Builder().strength(T).degree(60).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(FORBIDDEN_TUPLES).build(),
              new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(FORBIDDEN_TUPLES).build(),
              new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(FORBIDDEN_TUPLES).build(),
              new Spec.Builder().strength(T).degree(60).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(FORBIDDEN_TUPLES).build()
          );
        }
      }

      public static class Strength4Degree80 extends PeerJExperimentParameterized {
        private static final int T = 4;

        @Rule
        public Timeout timeout = new Timeout(60, MINUTES);

        public Strength4Degree80(Spec spec) {
          super(spec);
        }

        @Parameters
        public static List<Spec> parameters() {
          return Arrays.asList(
              new Spec.Builder().strength(T).degree(80).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(80).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(80).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(SOLVER).build()
          );
        }
      }

      public static class Strength5 extends PeerJExperimentParameterized {
        private static final int T = 5;

        @Rule
        public Timeout timeout = new Timeout(40, MINUTES);

        public Strength5(Spec spec) {
          super(spec);
        }

        @Parameters
        public static List<Spec> parameters() {
          return Arrays.asList(
              new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(SOLVER).build(),
              new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(SOLVER).build()
          );
        }
      }

      public static class Strength5ForbiddenTuples extends PeerJExperimentParameterized {
        private static final int T = 5;

        @Rule
        public Timeout timeout = new Timeout(40, MINUTES);

        public Strength5ForbiddenTuples(Spec spec) {
          super(spec);
        }

        @Parameters
        public static List<Spec> parameters() {
          return Arrays.asList(
              new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(FORBIDDEN_TUPLES).build(),
              new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(FORBIDDEN_TUPLES).build(),
              new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(FORBIDDEN_TUPLES).build(),
              new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(FORBIDDEN_TUPLES).build(),
              new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(FORBIDDEN_TUPLES).build(),
              new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(FORBIDDEN_TUPLES).build()
          );
        }
      }
    }
  }
}