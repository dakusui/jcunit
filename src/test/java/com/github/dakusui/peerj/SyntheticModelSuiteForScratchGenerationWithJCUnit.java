package com.github.dakusui.peerj;

import com.github.dakusui.peerj.model.ConstraintSet;
import com.github.dakusui.peerj.testbases.PeerJBase;
import com.github.dakusui.peerj.testbases.PeerJScratchWithJCUnit;
import org.junit.Rule;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static com.github.dakusui.peerj.model.ConstraintSet.BASIC;
import static com.github.dakusui.peerj.model.ConstraintSet.NONE;
import static com.github.dakusui.peerj.testbases.ExperimentBase.ConstraintHandlingMethod.SOLVER;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;

@RunWith(Enclosed.class)
public class SyntheticModelSuiteForScratchGenerationWithJCUnit {
  public static class Sandbox extends PeerJScratchWithJCUnit {
    public static final int T = 2;

    @Rule
    public Timeout timeout = new Timeout(10, MINUTES);

    public Sandbox(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return PeerJBase.parametersWith(2, SOLVER, 20, 80)
          .stream()
          .filter(each -> each.constraintSet() == BASIC)
          .collect(toList());
    }
  }

  public static class Strength2NoConstraint extends PeerJScratchWithJCUnit {
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
              .constraintSet(NONE)
              .constraintHandlingMethod(SOLVER)
              .build())
          .collect(toList());
    }
  }

  public static class Strength2BasicConstraint extends PeerJScratchWithJCUnit {
    public static final int T = 2;

    @Rule
    public Timeout timeout = new Timeout(10, MINUTES);

    public Strength2BasicConstraint(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return singletonList(
          new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(BASIC).constraintHandlingMethod(SOLVER).build()
      );
    }
  }

  public static class Strength2BasicPlusConstraint extends PeerJScratchWithJCUnit {
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

  public static class Strength3 extends PeerJScratchWithJCUnit {
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
              .constraintSet(NONE)
              .constraintHandlingMethod(SOLVER)
              .build())
          .collect(toList());
    }
  }

  public static class Strength4 extends PeerJScratchWithJCUnit {
    private static final int T = 4;

    @Rule
    public Timeout timeout = new Timeout(10, MINUTES);

    public Strength4(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return Arrays.asList(
          new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(NONE).constraintHandlingMethod(SOLVER).build(),
          new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(NONE).constraintHandlingMethod(SOLVER).build(),
          new Spec.Builder().strength(T).degree(60).rank(4).constraintSet(NONE).constraintHandlingMethod(SOLVER).build(),
          new Spec.Builder().strength(T).degree(80).rank(4).constraintSet(NONE).constraintHandlingMethod(SOLVER).build()
      );
    }
  }

  public static class Strength5 extends PeerJScratchWithJCUnit {
    private static final int T = 5;

    @Rule
    public Timeout timeout = new Timeout(10, MINUTES);

    public Strength5(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return Arrays.asList(
          new Spec.Builder().strength(T).degree(20).rank(4).constraintSet(NONE).constraintHandlingMethod(SOLVER).build(),
          new Spec.Builder().strength(T).degree(40).rank(4).constraintSet(NONE).constraintHandlingMethod(SOLVER).build(),
          new Spec.Builder().strength(T).degree(60).rank(4).constraintSet(NONE).constraintHandlingMethod(SOLVER).build()
      );
    }
  }

  public static class VSCA_2_3 extends PeerJScratchWithJCUnit {
    public VSCA_2_3(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return PeerJBase.parametersWith(2, 3, SOLVER, 20, 400);
    }
  }

  public static class VSCA_2_4 extends PeerJScratchWithJCUnit {
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


  public static class Debug extends PeerJScratchWithJCUnit {
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
