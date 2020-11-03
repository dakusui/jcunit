package com.github.dakusui.peerj;

import com.github.dakusui.peerj.model.ConstraintSet;
import com.github.dakusui.peerj.testbases.PeerJExperimentScratchParameterized;
import com.github.dakusui.peerj.ut.runners.PeerJExperimentIncrementalParameterized;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;

import static com.github.dakusui.peerj.testbases.PeerJExperimentBase.ConstraintHandlingMethod.FORBIDDEN_TUPLES;
import static com.github.dakusui.peerj.testbases.PeerJExperimentBase.ConstraintHandlingMethod.SOLVER;
import static java.util.concurrent.TimeUnit.MINUTES;

@RunWith(Enclosed.class)
public class IndustrialSimulationSuiteForScratchGeneration {
  public static class Strength2 extends PeerJExperimentScratchParameterized {
    public Strength2(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return PeerJExperimentIncrementalParameterized.parametersWith(2, SOLVER, 20, 400);
    }
  }

  /**
   * Constraint Handling by forbidden-tuple mode is not practical for the
   * "Industrial-scale" models because of its poor performance.
   */
  @Ignore
  public static class Strength2Cont extends PeerJExperimentScratchParameterized {
    public Strength2Cont(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return PeerJExperimentIncrementalParameterized.parametersWith(2, SOLVER, 400, 1000);
    }
  }

  public static class Strength2ForbiddenTuples extends PeerJExperimentScratchParameterized {
    public Strength2ForbiddenTuples(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return PeerJExperimentIncrementalParameterized.parametersWith(2, FORBIDDEN_TUPLES, 20, 400);
    }
  }

  public static class Strength3 extends PeerJExperimentScratchParameterized {
    public static final int T = 3;

    public Strength3(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return PeerJExperimentIncrementalParameterized.parametersWith(T, SOLVER, 20, 400);
    }
  }

  /**
   * Constraint Handling by forbidden-tuple mode is not practical for the
   * "Industrial-scale" models because of its poor performance.
   */
  @Ignore
  public static class Strength3ForbiddenTuples extends PeerJExperimentScratchParameterized {
    public static final int T = 3;

    public Strength3ForbiddenTuples(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return PeerJExperimentIncrementalParameterized.parametersWith(T, FORBIDDEN_TUPLES, 20, 400);
    }

  }

  public static class VSCA_2_3 extends PeerJExperimentScratchParameterized {
    public VSCA_2_3(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return PeerJExperimentIncrementalParameterized.parametersWith(2, 3, SOLVER, 20, 400);
    }
  }

  public static class VSCA_2_4 extends PeerJExperimentScratchParameterized {
    public VSCA_2_4(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return PeerJExperimentIncrementalParameterized.parametersWith(2, 4, SOLVER, 20, 100);
    }
  }

  public static class VSCA_2_4Cont extends PeerJExperimentScratchParameterized {
    public VSCA_2_4Cont(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return PeerJExperimentIncrementalParameterized.parametersWith(2, 4, SOLVER, 100, 180);
    }
  }

  public static class Strength4 extends PeerJExperimentScratchParameterized {
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

  public static class Strength5ForbiddenTuples extends PeerJExperimentScratchParameterized {
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

  public static class Strength4Degree80 extends PeerJExperimentScratchParameterized {
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

  public static class Strength5 extends PeerJExperimentScratchParameterized {
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

  public static class Strength4ForbiddenTuples extends PeerJExperimentScratchParameterized {
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
}
