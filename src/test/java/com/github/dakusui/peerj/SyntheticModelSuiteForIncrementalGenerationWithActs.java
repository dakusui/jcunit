package com.github.dakusui.peerj;

import com.github.dakusui.peerj.testbases.PeerJIncremental;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import java.util.List;

import static com.github.dakusui.peerj.testbases.ExperimentBase.ConstraintHandlingMethod.SOLVER;

@RunWith(Enclosed.class)
public class SyntheticModelSuiteForIncrementalGenerationWithActs {
  public static class Strength2 extends PeerJIncremental {
    public Strength2(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parametersWith(2, SOLVER, 20, 1000);
    }
  }

  public static class Strength3 extends PeerJIncremental {
    public Strength3(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parametersWith(3, SOLVER, 20, 340);
    }
  }

  public static class Strength3Cont extends PeerJIncremental {
    public Strength3Cont(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parametersWith(3, SOLVER, 340, 400);
    }
  }
}
