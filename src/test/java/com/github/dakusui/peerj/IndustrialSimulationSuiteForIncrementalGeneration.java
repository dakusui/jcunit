package com.github.dakusui.peerj;

import com.github.dakusui.peerj.ut.runners.PeerJExperimentIncrementalParameterized;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.List;

import static com.github.dakusui.peerj.testbases.PeerJExperimentBase.ConstraintHandlingMethod.SOLVER;

@RunWith(Enclosed.class)
public class IndustrialSimulationSuiteForIncrementalGeneration {
  public static class Strength2 extends PeerJExperimentIncrementalParameterized {
    public Strength2(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parametersWith(2, SOLVER, 20, 400);
    }
  }
}
