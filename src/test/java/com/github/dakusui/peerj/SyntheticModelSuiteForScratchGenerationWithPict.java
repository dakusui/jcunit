package com.github.dakusui.peerj;

import com.github.dakusui.peerj.model.ConstraintSet;
import com.github.dakusui.peerj.testbases.PeerJBase;
import com.github.dakusui.peerj.testbases.PeerJScratchWithPict;
import org.junit.Rule;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static com.github.dakusui.peerj.testbases.ExperimentBase.ConstraintHandlingMethod.SOLVER;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;

@RunWith(Enclosed.class)
public class SyntheticModelSuiteForScratchGenerationWithPict {
  public static class Strength2NoConstraint extends PeerJScratchWithPict {
    public static final int T = 2;

    @Rule
    public Timeout timeout = new Timeout(10, MINUTES);

    public Strength2NoConstraint(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return IntStream.range(1, 50)
          .mapToObj(i -> new Spec.Builder()
              .strength(T)
              .degree(i * 20)
              .rank(4)
              .constraintSet(ConstraintSet.NONE)
              .constraintHandlingMethod(SOLVER)
              .build())
          .collect(toList());
    }
  }

  public static class Strength2BasicConstraint extends PeerJScratchWithPict {
    public static final int T = 2;

    @Rule
    public Timeout timeout = new Timeout(10, MINUTES);

    public Strength2BasicConstraint(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return singletonList(
          new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.BASIC).constraintHandlingMethod(SOLVER).build()
      );
    }
  }

  public static class Strength2BasicPlusConstraint extends PeerJScratchWithPict {
    public static final int T = 2;

    @Rule
    public Timeout timeout = new Timeout(10, MINUTES);

    public Strength2BasicPlusConstraint(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return singletonList(
          new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(SOLVER).build()
      );
    }
  }

  public static class Strength3 extends PeerJScratchWithPict {
    public static final int T = 3;

    @Rule
    public Timeout timeout = new Timeout(10, MINUTES);

    public Strength3(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return IntStream.range(1, 20)
          .mapToObj(i -> new Spec.Builder()
              .strength(T)
              .degree(i * 20)
              .rank(4)
              .constraintSet(ConstraintSet.NONE)
              .constraintHandlingMethod(SOLVER)
              .build())
          .collect(toList());
    }
  }

  public static class Strength4 extends PeerJScratchWithPict {
    private static final int T = 4;

    @Rule
    public Timeout timeout = new Timeout(10, MINUTES);

    public Strength4(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return Arrays.asList(
          new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(SOLVER).build(),
          new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(SOLVER).build(),
          new Spec.Builder().strength(T).degree(60).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(SOLVER).build(),
          new Spec.Builder().strength(T).degree(80).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(SOLVER).build()
      );
    }
  }

  public static class Strength5 extends PeerJScratchWithPict {
    private static final int T = 5;

    @Rule
    public Timeout timeout = new Timeout(10, MINUTES);

    public Strength5(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return Arrays.asList(
          new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(SOLVER).build(),
          new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(SOLVER).build(),
          new Spec.Builder().strength(T).degree(60).rank(4).constraintSet(ConstraintSet.NONE).constraintHandlingMethod(SOLVER).build()
      );
    }
  }

  public static class VSCA_2_3 extends PeerJScratchWithPict {
    public VSCA_2_3(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return PeerJBase.parametersWith(2, 3, SOLVER, 20, 400);
    }
  }

  public static class VSCA_2_4 extends PeerJScratchWithPict {
    @Rule
    public Timeout timeout = new Timeout(10, MINUTES);

    public VSCA_2_4(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return PeerJBase.parametersWith(2, 4, SOLVER, 20, 180);
    }
  }


  public static class Debug extends PeerJScratchWithPict {
    private static final int T = 3;

    @Rule
    public Timeout timeout = new Timeout(10, MINUTES);

    public Debug(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return singletonList(
          new Spec.Builder().strength(T).degree(20).rank(3).constraintSet(ConstraintSet.BASIC_PLUS).constraintHandlingMethod(SOLVER).build()
      );
    }
  }
}
